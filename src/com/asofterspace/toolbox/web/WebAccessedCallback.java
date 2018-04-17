package com.asofterspace.toolbox.web;

/**
 * This interface describes a generic callback for having gotten content from the web
 *
 * @author Moya (a softer space, 2017)
 */
public interface WebAccessedCallback {

    /**
     * An error occurred during retrieval
     */
    void gotError();

    /**
     * The requested content has been retrieved
     * @param content  The content that was requested
     */
    void gotContent(String content);
}
