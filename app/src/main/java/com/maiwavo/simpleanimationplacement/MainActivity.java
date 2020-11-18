package com.maiwavo.simpleanimationplacement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.SkeletonNode;
import com.google.ar.sceneform.animation.ModelAnimator;
import com.google.ar.sceneform.rendering.AnimationData;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {

    private ArFragment fragment;
    private AnchorNode node;
    private ModelAnimator animator;
    private int nextAnim;
    private FloatingActionButton playBtn;
    private ModelRenderable renderable;
    private TransformableNode transNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.fragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        this.fragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
                if (renderable == null) {
                    return;
                }
                Anchor anchor = hitResult.createAnchor();
                if (node == null) {
                    node = new AnchorNode(anchor);
                    node.setParent(fragment.getArSceneView().getScene());

                    transNode = new TransformableNode(fragment.getTransformationSystem());
                    transNode.getScaleController().setMinScale(0.5f);
                    transNode.getScaleController().setMaxScale(1f);
                    transNode.setParent(node);
                    transNode.setRenderable(renderable);
                }
            }
        });

        this.fragment.getArSceneView().getScene().addOnUpdateListener(new Scene.OnUpdateListener() {
            @Override
            public void onUpdate(FrameTime frameTime) {
                if (node == null) {
                    if (playBtn.isEnabled()) {
                        playBtn.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                        playBtn.setEnabled(false);
                    }
                }
                else {
                    if (!playBtn.isEnabled()) {
                        playBtn.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.colorAccent));
                        playBtn.setEnabled(true);
                    }
                }
            }
        });

        this.playBtn = findViewById(R.id.playBtn);
        this.playBtn.setEnabled(false);
        this.playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (animator == null || !animator.isRunning()) {
                    Log.println(Log.ASSERT, "second", renderable.getAnimationDataCount() + "");
                    Log.println(Log.ASSERT, "second", renderable.getBoneCount() + "");
                    AnimationData data = renderable.getAnimationData(nextAnim);
                    nextAnim = ((nextAnim + 1) % renderable.getAnimationDataCount());
                    animator = new ModelAnimator(data, renderable);
                    animator.start();
                }
            }
        });

        setupModel();


    }

    private void setupModel() {
        ModelRenderable.builder()
                .setSource(this, Uri.parse("skeleton.sfb"))
                .build()
                .thenAccept(renderableNew -> renderable = renderableNew)
                .exceptionally(throwable -> {
                    Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    return null;

                });
    }
}