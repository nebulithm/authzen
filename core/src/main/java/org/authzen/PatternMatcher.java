package org.authzen;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

public class PatternMatcher {
    
    public boolean matches(String pattern, String value) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        return matcher.matches(Paths.get(value));
    }
}
