package io.gitlab.innom.jses.core.exception;

import io.gitlab.innom.jses.core.SearchEngine;

public class CaptchaPromptException extends Exception {

    public CaptchaPromptException(SearchEngine engine) {
        super(String.format("Captcha prompt while searching on %s", engine.name()));
    }

}
