package com.github.arsenmonets.refactoringproject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingVolume;
import com.jme3.material.MatParam;
import com.jme3.material.MaterialDef;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.shader.VarType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import сom.github.arsenmonets.refactoringproject.refactored.core.GameSession;
import сom.github.arsenmonets.refactoringproject.refactored.objectmanagers.ObstacleManager;
import сom.github.arsenmonets.refactoringproject.refactored.objectmanagers.PlayerManager;
import сom.github.arsenmonets.refactoringproject.refactored.themes.ThemeManager;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ObstacleManagerTest {

    @Mock private Node rootNode;
    @Mock private AssetManager assetManager;
    @Mock private PlayerManager playerManager;
    @Mock private GameSession session;
    @Mock private ThemeManager themeManager;
    @Mock private Geometry prototypeObstacle; 

    private ObstacleManager obstacleManager;

    private static final int MIN_DISTANCE_X = 30;
    private static final int MAX_DISTANCE_X = 90;
    private static final int Z_SPREAD = 50;
    private static final float CLEANUP_THRESHOLD = 10f;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void init() {
        lenient().when(prototypeObstacle.clone()).thenAnswer(invocation -> {
            Geometry clonedGeometry = mock(Geometry.class);
            when(clonedGeometry.getLocalTranslation()).thenReturn(new Vector3f()); 
            return clonedGeometry;
        });

        MaterialDef mockDef = mock(MaterialDef.class);
        MatParam colorParam = new MatParam(VarType.Vector4, "Color", null);
        lenient().when(mockDef.getMaterialParam("Color")).thenReturn(colorParam);
        lenient().when(assetManager.loadAsset(any(AssetKey.class))).thenReturn(mockDef);

        obstacleManager = new ObstacleManager(
                rootNode,
                assetManager,
                playerManager,
                session,
                themeManager,
                prototypeObstacle,
                MIN_DISTANCE_X,
                MAX_DISTANCE_X,
                Z_SPREAD,
                CLEANUP_THRESHOLD
        );
    }
    
    private void injectActiveObstacles(List<Geometry> obstacles) {
        try {
            java.lang.reflect.Field field = ObstacleManager.class.getDeclaredField("activeObstacles");
            field.setAccessible(true);
            field.set(obstacleManager, obstacles);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to inject activeObstacles list: " + e.getMessage());
        }
    }

    @Test
    void testCheckCollisionsDetectsCollision() {
        BoundingVolume playerBounds = mock(BoundingVolume.class);
        when(playerManager.getCollisionBounds()).thenReturn(playerBounds);

        Geometry obstacle1 = mock(Geometry.class);
        BoundingVolume obstacle1Bounds = mock(BoundingVolume.class);
        when(obstacle1.getWorldBound()).thenReturn(obstacle1Bounds);
        when(playerBounds.intersects(obstacle1Bounds)).thenReturn(false);

        Geometry obstacle2 = mock(Geometry.class);
        BoundingVolume obstacle2Bounds = mock(BoundingVolume.class);
        when(obstacle2.getWorldBound()).thenReturn(obstacle2Bounds);
        when(playerBounds.intersects(obstacle2Bounds)).thenReturn(true);

        List<Geometry> activeObstacles = new ArrayList<>();
        activeObstacles.add(obstacle1);
        activeObstacles.add(obstacle2);
        injectActiveObstacles(activeObstacles);

        boolean collisionDetected = obstacleManager.checkCollisions();

        assertTrue(collisionDetected);
        verify(playerManager).getCollisionBounds();
        verify(obstacle1).getWorldBound();
        verify(obstacle2).getWorldBound();
    }

    @Test
    void testCheckCollisionsNoCollision() {
        BoundingVolume playerBounds = mock(BoundingVolume.class);
        when(playerManager.getCollisionBounds()).thenReturn(playerBounds);

        Geometry obstacle1 = mock(Geometry.class);
        BoundingVolume obstacle1Bounds = mock(BoundingVolume.class);
        when(obstacle1.getWorldBound()).thenReturn(obstacle1Bounds);
        when(playerBounds.intersects(obstacle1Bounds)).thenReturn(false);

        List<Geometry> activeObstacles = new ArrayList<>();
        activeObstacles.add(obstacle1);
        injectActiveObstacles(activeObstacles);

        boolean collisionDetected = obstacleManager.checkCollisions();

        assertFalse(collisionDetected);
    }

    @Test
    void testSpawnIfNeededSpawnsObstacles() {
        when(session.getSpawnAreaScale()).thenReturn(2); 
        when(playerManager.getLocation()).thenReturn(Vector3f.ZERO);
        when(themeManager.isCurrentThemeWireframe()).thenReturn(false);
        when(themeManager.getRandomObstacleColor()).thenReturn(ColorRGBA.Blue);

        obstacleManager.update(); 
        obstacleManager.update(); 

        verify(rootNode, times(2)).attachChild(any(Geometry.class));
    }

    @Test
    void testSpawnIfNeededDoesNotSpawnIfEnoughObstacles() {
        when(session.getSpawnAreaScale()).thenReturn(1); 
        when(playerManager.getLocation()).thenReturn(Vector3f.ZERO); 
        when(themeManager.isCurrentThemeWireframe()).thenReturn(false);
        when(themeManager.getRandomObstacleColor()).thenReturn(ColorRGBA.Blue);

        obstacleManager.update();
        verify(rootNode, times(1)).attachChild(any(Geometry.class));

        obstacleManager.update(); 
        verify(rootNode, times(1)).attachChild(any(Geometry.class));
    }

    @Test
    void testCleanupRemovesOffscreenObstacles() {
        Vector3f playerLocation = new Vector3f(100, 0, 0);
        when(playerManager.getLocation()).thenReturn(playerLocation);

        Geometry obstacleToKeep = mock(Geometry.class);
        when(obstacleToKeep.getLocalTranslation()).thenReturn(new Vector3f(playerLocation.x + 5, 0, 0));
        
        Geometry obstacleToRemove = mock(Geometry.class);
        when(obstacleToRemove.getLocalTranslation()).thenReturn(new Vector3f(playerLocation.x - CLEANUP_THRESHOLD - 1, 0, 0));

        List<Geometry> activeObstacles = new ArrayList<>();
        activeObstacles.add(obstacleToKeep);
        activeObstacles.add(obstacleToRemove);
        injectActiveObstacles(activeObstacles);

        obstacleManager.update(); 

        verify(obstacleToRemove).removeFromParent();
        verify(obstacleToKeep, never()).removeFromParent();
        assertEquals(1, activeObstacles.size());
    }

    @Test
    void testClearRemovesAllObstacles() {
        Geometry obstacle1 = mock(Geometry.class);
        Geometry obstacle2 = mock(Geometry.class);

        List<Geometry> activeObstacles = new ArrayList<>();
        activeObstacles.add(obstacle1);
        activeObstacles.add(obstacle2);
        injectActiveObstacles(activeObstacles);

        obstacleManager.clear();

        verify(obstacle1).removeFromParent();
        verify(obstacle2).removeFromParent();
        assertTrue(activeObstacles.isEmpty());
    }
}