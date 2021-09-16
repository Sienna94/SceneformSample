package com.example.sceneformsample

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sceneformsample.databinding.ActivityMainBinding
import com.example.sceneformsample.utils.ArManager
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ux.ArFragment
import java.lang.NumberFormatException

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var arFragment: ArFragment

    //    private val model: Uri = Uri.parse("andy.sfb")
    private val fightModel: Uri = Uri.parse("model_fight.sfb")
    private val duck: String =
        "https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/Duck/glTF/Duck.gltf"
    private val brainstem: String =
        "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/BrainStem/glTF/BrainStem.gltf"
    private val movingCube: String =
        "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/AnimatedCube/glTF/AnimatedCube.gltf"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment
        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                return@setOnTapArPlaneListener
            }
        }
        //to show current camera position, add listener to AR Fragment
        arFragment.arSceneView.scene.addOnUpdateListener {
            if (ArManager.camera == null) {
                ArManager.camera = arFragment.arSceneView.arFrame?.camera!!
            }
            arFragment.onUpdate(it)
            showCameraPosition(ArManager.camera!!.pose)
            val cameraPose = arFragment.arSceneView.arFrame?.camera!!.pose
            ArManager.rotateContent(cameraPose)
        }
        binding.btnRotate.setOnClickListener(this)
        binding.btnAnimate.setOnClickListener(this)
        binding.btnFighter.setOnClickListener(this)
        binding.btn180.setOnClickListener(this)
        binding.btn270.setOnClickListener(this)
        binding.btn360.setOnClickListener(this)
        binding.btn90.setOnClickListener(this)
        binding.btnBox.setOnClickListener(this)
        binding.btnDuck.setOnClickListener(this)
        binding.btnRobot.setOnClickListener(this)
        binding.btnClear.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    private fun showCameraPosition(pose: Pose) {
        binding.tvCameraPos.text = "X = ${pose.tx()}\n" +
                "Y = ${pose.ty()}\n" +
                "Z = ${pose.tz()}"
    }

    @SuppressLint("SetTextI18n")
    private fun showAnchorPos(anchor: Anchor) {
        binding.tv.text = "X = ${anchor.pose.tx()}\n " +
                "Y = ${anchor.pose.ty()}\n" +
                "Z = ${anchor.pose.tz()}"
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnRotate -> {
                try {
                    val str = binding.et.text.toString().trim()
                    val degree = str.toFloat()

                    if (str.isNotEmpty() && ArManager.testNode == null) {
                        val testAnchor =
                            ArManager.createAnchor(arFragment, 0f, -1f, -1f, 0f, 0f, 0f, 0f)
                        ArManager.placeTestNode(this, arFragment, testAnchor, "test")
                        ArManager.testNode?.let { ArManager.rotateContent(it, degree) }
                    } else if (str.isNotEmpty() && ArManager.testNode != null) {
                        ArManager.rotateContent(ArManager.testNode!!, degree)
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Error : $e", Toast.LENGTH_SHORT).show()
                }
            }
            binding.btnFighter -> {
                val testAnchor = ArManager.createAnchor(arFragment, 0f, -1f, -1f, 0f, 0f, 0f, 0f)
                ArManager.placeObject(this, arFragment, testAnchor, fightModel)
            }
            binding.btnAnimate -> {
                if (ArManager.animatedRenderable != null) {
                    ArManager.animateModel(ArManager.animatedRenderable!!)
                } else {
                    Toast.makeText(this, "no possible renderable", Toast.LENGTH_SHORT).show()
                    return
                }
            }

            binding.btn90 -> {
                val testAnchor = ArManager.createAnchor(arFragment, 1f, 0f, 0f, 0f, 0f, 0f, 0f)
                ArManager.placeContent(this, arFragment, testAnchor, "90")
                showAnchorPos(testAnchor)
            }
            binding.btn180 -> {
                val testAnchor =
                    ArManager.createAnchor(arFragment, 0f, -0.2f, 0.3f, 0f, 0f, 0f, 0f)
                ArManager.placeContent(this, arFragment, testAnchor, "180")
                showAnchorPos(testAnchor)
            }
            binding.btn270 -> {
                val testAnchor =
                    ArManager.createAnchor(arFragment, -1f, 0.5f, 0f, 0f, 0f, 0f, 0f)
                ArManager.placeContent(this, arFragment, testAnchor, "270")
                showAnchorPos(testAnchor)
            }
            binding.btn360 -> {
                val testAnchor =
                    ArManager.createAnchor(arFragment, 0f, 0.5f, -1f, 0f, 0f, 0f, 0f)
                ArManager.placeContent(this, arFragment, testAnchor, "360")
                showAnchorPos(testAnchor)
            }
            binding.btnBox -> {
                val testAnchor =
                    ArManager.createAnchor(arFragment, 1f, 0.1f, -1f, 0f, 0f, 0f, 0f)
                ArManager.placeObject(this, arFragment, testAnchor, movingCube)
                showAnchorPos(testAnchor)
            }
            binding.btnDuck -> {
                val testAnchor =
                    ArManager.createAnchor(arFragment, -1f, 0f, 1f, 0f, 0f, 0f, 0f)
                ArManager.placeObject(this, arFragment, testAnchor, duck)
                showAnchorPos(testAnchor)
            }
            binding.btnRobot -> {
                val testAnchor =
                    ArManager.createAnchor(arFragment, -1f, 0.15f, -1f, 0f, 0f, 0f, 0f)
                ArManager.placeObject(this, arFragment, testAnchor, brainstem)
                showAnchorPos(testAnchor)
            }
            binding.btnClear -> {
                ArManager.clearScene(arFragment)
            }
        }
    }
}