package com.example.sceneformsample.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.sceneformsample.databinding.ContentBinding
import com.google.ar.core.Anchor
import com.google.ar.core.Camera
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Sun
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*

object ArManager {
    var nodeList = ArrayList<TransformableNode>()
    var camera: Camera? = null
    var downwardNode: TransformableNode? = null
    var testNode: TransformableNode? = null
    private lateinit var modelAnimator: ModelAnimator
    var animatedRenderable: ModelRenderable? = null

    private fun addNodeToScene(
        fragment: ArFragment,
        anchor: Anchor,
        renderable: Renderable,
        downward: Boolean
    ): TransformableNode {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)

        node.renderable = renderable
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)

        if (!downward) {
            nodeList.add(node)
        }

        return node
    }

    private fun addNodeToScene(
        fragment: ArFragment,
        anchor: Anchor,
        renderable: ModelRenderable,
        downward: Boolean
    ): TransformableNode {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)

        node.renderable = renderable
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)

        if (!downward) {
            nodeList.add(node)
        }

        return node
    }

    fun createAnchor(
        fragment: ArFragment,
        x: Float,
        y: Float,
        z: Float,
        qx: Float,
        qy: Float,
        qz: Float,
        qw: Float,
    ): Anchor {
        val session: Session = fragment.arSceneView.session!!
        val translation: FloatArray = floatArrayOf(x, y, z)
        val rotation: FloatArray = floatArrayOf(qx, qy, qz, qw)

        return session.createAnchor(
            Pose(translation, rotation)
        )
    }

    fun placeObject(context: Context, fragment: ArFragment, anchor: Anchor, model: Uri) {
        ModelRenderable.builder()
            .setSource(fragment.context, model)
            .build()
            .thenAccept {
                addNodeToScene(fragment, anchor, it, false)
                animatedRenderable = it
            }
            .exceptionally {
                val builder = AlertDialog.Builder(context)
                builder.setMessage(it.message).setTitle("Error")
                val dialog = builder.create()
                dialog.show()
                null
            }
    }

    fun placeObject(context: Context, fragment: ArFragment, anchor: Anchor, modelurl: String) {
        ModelRenderable.builder()
            .setSource(
                context, RenderableSource.builder().setSource(
                    context,
                    Uri.parse(modelurl),
                    RenderableSource.SourceType.GLTF2
                )
                    .setScale(0.1f)
                    .build()
            )
            .setRegistryId(modelurl)
            .build()
            .thenAccept {
                addNodeToScene(fragment, anchor, it, false)
            }
            .exceptionally {
                Toast.makeText(context, "Unable to load renderable", Toast.LENGTH_LONG).show()
                Log.d("doja", "placeObject Error: $it")
                null
            }
    }

    fun placeContent(context: Context, fragment: ArFragment, anchor: Anchor, name: String) {
        val contentLayout = ContentBinding.inflate(LayoutInflater.from(context)).apply {
            this.textview.text = name
            this.btnYes.setOnClickListener {
                Toast.makeText(context, "yes", Toast.LENGTH_SHORT).show()
            }
            this.btnNo.setOnClickListener {
                Toast.makeText(context, "no", Toast.LENGTH_SHORT).show()
            }
        }

        ViewRenderable.builder()
            .setView(context, contentLayout.root)
            .build()
            .thenAccept {
                val node = addNodeToScene(
                    fragment,
                    anchor,
                    it,
                    true
                )
                rotateContent(node, name.toFloat())
            }
    }

    fun placeTestNode(context: Context, fragment: ArFragment, anchor: Anchor, name: String){
        val contentLayout = ContentBinding.inflate(LayoutInflater.from(context)).apply {
            this.textview.text = name
            this.btnYes.setOnClickListener {
                Toast.makeText(context, "TEST yes", Toast.LENGTH_SHORT).show()
            }
            this.btnNo.setOnClickListener {
                Toast.makeText(context, "TEST no", Toast.LENGTH_SHORT).show()
            }
        }

        ViewRenderable.builder()
            .setView(context, contentLayout.root)
            .build()
            .thenAccept {
                testNode = addNodeToScene(
                    fragment,
                    anchor,
                    it,
                    true
                )
            }
    }

    fun animateModel(modelRenderable: ModelRenderable) {
        val animationCount = modelRenderable.animationDataCount
        if (animationCount > 0) {
            val animationData = modelRenderable.getAnimationData(0)
            modelAnimator = ModelAnimator(animationData, modelRenderable).apply {
                this.repeatCount = 3
                this.start()
            }
        }
    }

    fun placeDownward(
        context: Context,
        fragment: ArFragment,
        anchor: Anchor,
        name: String,
        angle: Float
    ) {
        val contentLayout = ContentBinding.inflate(LayoutInflater.from(context))
        contentLayout.textview.text = name

        ViewRenderable.builder()
            .setView(context, contentLayout.root)
            .build()
            .thenAccept {
                downwardNode = addNodeToScene(fragment, anchor, it, true).apply {
                    this.setOnTapListener { hitTestResult, motionEvent ->
                        Toast.makeText(context, "name = $name", Toast.LENGTH_SHORT).show()
                    }
                    this.localRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), -angle)
                }
            }
    }


    fun rotateContent(cameraPose: Pose) {
        // node should be transformable node, to rotate after it's created
        camera?.let {
            val cameraAngle = getAngle(cameraPose)
            Log.d("doja", "getYaw: $cameraAngle")
            for (node in nodeList) {
                node.localRotation = Quaternion.axisAngle(
                    Vector3(0f, 1f, 0f),
                    cameraAngle
                )
            }
        }
    }

    fun rotateContent(node: Node, angle: Float) {
        node.localRotation = Quaternion.axisAngle(
            Vector3(0f, 1f, 0f),
            angle
        )
    }

    private fun getAngle(pose: Pose): Float {
        val qw = pose.qw()
        val qx = pose.qx()
        val qy = pose.qy()
        val qz = pose.qz()
        return (atan2(
            2 * (qw * qy + qx * qz).toDouble(),
            1 - 2 * (qy.toDouble().pow(2.0) + qx.toDouble().pow(2.0))
        ) * (180.0 / Math.PI)).toFloat()
    }

    fun removeAnchorNode(fragment: ArFragment, node: AnchorNode) {
        fragment.arSceneView.scene.removeChild(node)
        node.anchor?.detach()
        node.setParent(null)
        node.renderable = null
    }

    fun clearScene(fragment: ArFragment) {
        val childern = fragment.arSceneView.scene.children
        for (child in childern) {
            if (child is AnchorNode) {
                child.anchor?.detach()
            } else if (child is Camera && child is Sun) {
                child.setParent(null)
            }
        }
    }
}