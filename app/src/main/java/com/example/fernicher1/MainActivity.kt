package com.example.fernicher1

import android.graphics.Color.RED
import android.graphics.Color.WHITE
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.CompletableFuture

private const val PEEK = 50f

class MainActivity : AppCompatActivity() {
    private val models = mutableListOf(
        Model(R.drawable.chair, "Chair", R.raw.chair),
        Model(R.drawable.oven, "Oven", R.raw.oven),
        Model(R.drawable.piano, "Piano", R.raw.piano),
        Model(R.drawable.table, "Table", R.raw.table)
    )
    lateinit var arFragment: ArFragment
    lateinit var selectedModel: Model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arFragment=fragment as ArFragment
        setupButtomSheet()
        setupRecyclerView()
        setupDoubleTapArPlaneListner()
    }

    private fun setupDoubleTapArPlaneListner(){
        arFragment.setOnTapArPlaneListener { hitResult, _, _t ->
            loadModel { modelRenderable, viewRenderable ->
                addNodeToSence(hitResult.createAnchor(),modelRenderable,viewRenderable)
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

    private fun getCurrentScene()=arFragment.arSceneView.scene

    private fun addNodeToSence(               // locate the object anh his view in the ARsence
        anchor: Anchor,                        // anchore point, fill the object is part of the scene
        modelRenderable: ModelRenderable,
        viewRenderable: ViewRenderable
    ) {
        val anchorNode=AnchorNode(anchor)         //anchor node is the parent node
        val modelNode=TransformableNode(arFragment.transformationSystem).apply {
            renderable=modelRenderable
            setParent(anchorNode)                      // case that his parents will be anchor node
            getCurrentScene().addChild(anchorNode)
            select()
        }
        val viewNode=Node().apply {      // its include the delete buttom
            renderable=null
            setParent(modelNode)           // his parent is modelNode
            val box=modelNode.renderable?.collisionShape as Box
            localPosition= Vector3(0f,box.size.y,0f)  // it value of x,y,z locale cordinate mesure from anchor node
            (viewRenderable.view as Button).setOnClickListener {  // in a case when you press delete buttom
                getCurrentScene().removeChild(anchorNode)
            }
        }
      modelNode.setOnTapListener{_,_->
        if (!modelNode.isTransforming){
        if (viewNode.renderable==null){
            viewNode.renderable=viewRenderable
        }else{
            viewNode.renderable=null
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
            .setSource(this, selectedModel.modelResourceId)  //appear in the raw directory, creat the object
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
}
