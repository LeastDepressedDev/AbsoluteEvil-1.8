package me.qigan.render;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    //Sources
    private final String vertex;
    private final String fragment;

    //Pointers? I hope so
    private int vertexID = 0, fragmentID = 0;

    public final int shaderProgram;

    public Shader(String vertex, String fragment) {
        this.vertex = vertex;
        this.fragment = fragment;

        if (this.vertex != null) {
            this.vertexID = glCreateShader(GL_VERTEX_SHADER);
            glShaderSource(vertexID, vertex);
            glCompileShader(vertexID);
        }

        if (this.fragment != null) {
            this.fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
            glShaderSource(fragmentID, fragment);
            glCompileShader(fragmentID);
        }

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexID);
        glAttachShader(shaderProgram, fragmentID);
        glLinkProgram(shaderProgram);
    }

    public int getVertexID() {
        return vertexID;
    }

    public int getFragmentID() {
        return fragmentID;
    }
}
