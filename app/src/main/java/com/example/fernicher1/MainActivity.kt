package com.example.fernicher1

import android.graphics.Color.RED
import android.graphics.Color.WHITE
import android.media.CamcorderProfile
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.PlaneRenderer
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseTransformableNode
import com.google.ar.sceneform.ux.SelectionVisualizer
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.CompletableFuture

private const val PEEK = 50f
private const val DOUBLE_TAB_TOLERANCE_MS = 1000L

class MainActivity : AppCompatActivity() {
    lateinit var arFragment: ArFragment
    lateinit var selectedModel: Model
    private val viewNodes = mutableListOf<Node>()
    private lateinit var photoSaver: PhotoSaver
    private lateinit var videoRecorder: VideoRecorder
    private var isRecording = false

    private val models = mutableListOf(
        Model(R.drawable.chair, "Chair", R.raw.chair),
        Model(R.drawable.oven, "Oven", R.raw.oven),
        Model(R.drawable.piano, "Piano", R.raw.piano),
        Model(R.drawable.table, "Table", R.raw.table)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arFragment = fragment as ArFragment
        if (arFragment!=null){
            arFragment.transformationSystem.selectionVisualizer = CustemVisualizer()
        }

        setupFab()
        setupButtomSheet()
        setupRecyclerView()
        setupDoubleTapArPlaneListener()
        photoSaver = PhotoSaver(this)
        videoRecorder = VideoRecorder(this).apply {
            sceneView = arFragment.arSceneView

            setVideoQuality(CamcorderProfile.QUALITY_1080P, resources.configuration.orientation)

        }
        setupFab()
        getCurrentScene().addOnUpdateListener {
            rotateViewNodesTowardUser()
        }
    }

    class CustemVisualizer:SelectionVisualizer{
        override fun applySelectionVisual(node: BaseTransformableNode?) {  }

        override fun removeSelectionVisual(node: BaseTransformableNode?) { }

    }

    private fun setupFab() {

        fab.setOnClickListener {

            if (!isRecording) {

                photoSaver.takePhoto(arFragment.arSceneView)

            }

        }

        fab.setOnLongClickListener {

            isRecording = videoRecorder.toggleRecordingState()

            true

        }

        fab.setOnTouchListener { view, motionEvent ->

            if (motionEvent.action == MotionEvent.ACTION_UP && isRecording) {

                isRecording = videoRecorder.toggleRecordingState()

                Toast.makeText(this, "Saved video to gallery!", Toast.LENGTH_LONG).show()

                true

            } else false

        }

    }

    private fun setupDoubleTapArPlaneListener() {
        var firstTapTime = 0L

        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            if (firstTapTime == 0L) {
                firstTapTime = System.currentTimeMillis()
            } else if (System.currentTimeMillis() - firstTapTime < DOUBLE_TAB_TOLERANCE_MS) {

                firstTapTime = 0
                loadModel { modelRenderable, viewRenderable ->
                    addNodeToSence(hitResult.createAnchor(), modelRenderable, viewRenderable)

                    arFragment.arSceneView.planeRenderer.isVisible = false
                    arFragment.planeDiscoveryController.hide()
                    arFragment.planeDiscoveryController.setInstructionView(null)

                   /* arFragment.arSceneView.planeRenderer.material.thenAccept {
                        it.setTexture(PlaneRenderer.MATERIAL_TEXTURE,)
                    }*/
                }

            } else {
                firstTapTime = System.currentTimeMillis()
            }
        }
    }

    private fun setupRecyclerView() {
        rvModels.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvModels.adapter = ModelAdapter(models).apply {
            selectedModl.observe(this@MainActivity, Observer {
                this@MainActivity.selectedModel = it
                val newTitle = "Models (${it.title})"
                tvModel.text = newTitle
            })
        }
    }


    private fun setupButtomSheet() {
        val buttonSheetBehaviour = BottomSheetBehavior.from(bootomSheet)
        buttonSheetBehaviour.peekHeight =  //from dp to pixel
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, PEEK, resources.displayMetrics
            ).toInt()

    }

    private fun getCurrentScene() = arFragment.arSceneView.scene

    private fun rotateViewNodesTowardUser() {
        for (node in viewNodes) {                    // evey nodes that rebderable in the view
            node.renderable?.let {
                val camPos = getCurrentScene().camera.worldPosition     // camera new position
                val viewNodePos = node.worldPosition                    // view node position
                val dir = Vector3.subtract(camPos, viewNodePos)          // the gap between them
                node.worldRotation =
                    Quaternion.lookRotation(dir, Vector3.up())    // move the view node
            }
        }
    }

    private fun addNodeToSence(               // locate the object and his view in the ARscence
        anchor: Anchor,                        // anchore point, when moving the camera the model stay like a static furnither in the room
        modelRenderable: ModelRenderable,
        viewRenderable: ViewRenderable
    ) {
        val anchorNode = AnchorNode(anchor)         //anchor node is the parent node
        val modelNode = TransformableNode(arFragment.transformationSystem).apply {
            renderable = modelRenderable
            setParent(anchorNode)                      // case that his parents will be anchor node
            getCurrentScene().addChild(anchorNode)

            select()
        }
        val viewNode = Node().apply {      // its include the delete buttom
            renderable =
                null               // will not seen initially-at first,when we tap on the model it become seen
            setParent(modelNode)           // his parent is modelNode
            //so the hierarchy anchorNode->modelNode->viewNode
            val box =
                modelNode.renderable?.collisionShape as Box   // now we want to locate this buttom, Box mean x,y,z
            localPosition = Vector3(         // local coordinate in x,y,z from anchor node
                0f,
                box.size.y,
                0f
            )
            (viewRenderable.view as Button).setOnClickListener {  // in a case when you press delete buttom
                getCurrentScene().removeChild(anchorNode)
                viewNodes.remove(this)
            }
        }
        viewNodes.add(viewNode)
        modelNode.setOnTapListener { _, _ ->   // if you click on the model and the button not show it will showed and vice verscia
            if (!modelNode.isTransforming) {   // not in a move
                if (viewNode.renderable == null) {
                    viewNode.renderable = viewRenderable
                } else {
                    viewNode.renderable = null
                }
            }
        }
    }


    private fun createDeleteButton(): Button {
        return Button(this).apply {
            text = "Delete"
            setBackgroundColor(RED)
            setTextColor(WHITE)
        }
    }

    private fun loadModel(callback: (ModelRenderable, ViewRenderable) -> Unit) { // the fun get callback from herself
        val modelRenderable = ModelRenderable.builder()
            .setSource(
                this,
                selectedModel.modelResourceId
            )  //appear in the raw directory, creat the object
            .build()
        val viewRenderable = ViewRenderable.builder()
            .setView(this, createDeleteButton())            //add delete buttom to the object
            .build()
        CompletableFuture.allOf(modelRenderable, viewRenderable)
            .thenAccept {
                callback(modelRenderable.get(), viewRenderable.get())
            }
            .exceptionally {
                Toast.makeText(this, "Some Error->${it.message}", Toast.LENGTH_LONG).show()
                null
            }
    }

    //when their is one modelRenderable without viewRenderable
    private fun loadModel1(callback: (ModelRenderable) -> Unit) { // the fun get callback from herself
        val modelRenderable = ModelRenderable.builder()
            .setSource(
                this,
                selectedModel.modelResourceId
            )  //appear in the raw directory, creat the object
            .build()
        CompletableFuture.allOf(modelRenderable)
            .thenAccept {
                callback(modelRenderable.get())
            }
            .exceptionally {
                Toast.makeText(this, "Some Error->${it.message}", Toast.LENGTH_LONG).show()
                null
            }
    }
}
