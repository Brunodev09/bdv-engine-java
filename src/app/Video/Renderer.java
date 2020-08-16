package app.Video;

import app.Entities.Entity;
import app.Math.MatrixUtils;
import app.Models.Model;
import app.Models.TexturedModel;
import app.Shaders.DefaultShader;
import app.Shaders.GeometryShader;
import app.Texture.ModelTexture;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.util.List;
import java.util.Map;

public class Renderer {

    private Matrix4f _projection;
    private DefaultShader _shader;
    private GeometryShader _geoShader;

    public Renderer() {

    }

    public Renderer(DefaultShader shader, Matrix4f projectionMatrix) {
        _shader = shader;
        // Culling back faces
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        shader.init();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }

    public Renderer(GeometryShader shader) {
        // Culling back faces
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        shader.init();
        shader.loadProjectionMatrix(_projection);
        shader.stop();
    }

    public Renderer(GeometryShader shader, Matrix4f orthographicMatrix) {
        this._projection = orthographicMatrix;
        _geoShader = shader;
        shader.init();
        shader.loadProjectionMatrix(this._projection);
        shader.stop();
    }

    public void render(Model mdl) {
        GL30.glBindVertexArray(mdl.getVAOID());
        GL20.glEnableVertexAttribArray(0);
//        Used only if we are not indexing the vertexes
//        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, mdl.getVertexCount());
        GL11.glDrawElements(GL11.GL_TRIANGLES, mdl.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
    }

    public void render(TexturedModel tmdl) {
        Model mdl = tmdl.getModel();
        GL30.glBindVertexArray(mdl.getVAOID());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tmdl.getModelTexture().getId());
        GL11.glDrawElements(GL11.GL_TRIANGLES, mdl.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL30.glBindVertexArray(0);
    }

    public void renderEntities(Map<TexturedModel, List<Entity>> entities) {
        for (TexturedModel key : entities.keySet()) {
            this._modelSetup(key);
            List<Entity> entitiesToLoadFromModel = entities.get(key);
            for (Entity entity : entitiesToLoadFromModel) {
                this._applyTransformationAndLoadIntoShader(entity);
                GL11.glDrawElements(GL11.GL_TRIANGLES, key.getModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
            }
            this._unbindTexture();
        }
    }

    public void renderEntities2D(Map<TexturedModel, List<Entity>> entities) {
        for (TexturedModel key : entities.keySet()) {
            this._modelSetup2D(key);
            List<Entity> entitiesToLoadFromModel = entities.get(key);
            for (Entity entity : entitiesToLoadFromModel) {
                this._applyTransformationAndLoadIntoShader2D(entity);
                GL11.glDrawElements(GL11.GL_TRIANGLES, key.getModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
            }
            this._unbindTexture2D();
        }
    }


    private void _modelSetup(TexturedModel tmdl) {
        Model mdl = tmdl.getModel();
        GL30.glBindVertexArray(mdl.getVAOID());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        ModelTexture md = tmdl.getModelTexture();
        _shader.loadSpecularLights(md.getShineDamper(), md.getReflectivity());

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tmdl.getModelTexture().getId());
    }

    private void _modelSetup2D(TexturedModel tmdl) {
        Model mdl = tmdl.getModel();
        GL30.glBindVertexArray(mdl.getVAOID());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tmdl.getModelTexture().getId());
    }

    private void _unbindTexture() {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);

        GL30.glBindVertexArray(0);
    }

    private void _unbindTexture2D() {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);

        GL30.glBindVertexArray(0);
    }

    private void _applyTransformationAndLoadIntoShader(Entity entity) {
        // Applying transformations and loading them to the VAO
        Matrix4f transformationMatrix = MatrixUtils.createTransformationMatrix(
                entity.getPosition(),
                entity.getRotX(),
                entity.getRotY(),
                entity.getRotZ(),
                entity.getScale());
        _shader.loadTransformationMatrix(transformationMatrix);
    }

    private void _applyTransformationAndLoadIntoShader2D(Entity entity) {
        Matrix4f transformationMatrix = MatrixUtils.createTransformationMatrix(
                entity.getPosition(),
                entity.getRotX(),
                entity.getRotY(),
                entity.getRotZ(),
                entity.getScale());
        _geoShader.loadTransformationMatrix(transformationMatrix);
    }
}