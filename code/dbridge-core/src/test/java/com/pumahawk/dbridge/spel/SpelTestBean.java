package com.pumahawk.dbridge.spel;

import org.springframework.stereotype.Component;

@Component
public class SpelTestBean {
    public String setPrefix(String prefix, String string) {
        return prefix != null
            ? prefix.concat(string)
            : string;
    }
}
