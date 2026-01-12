package com.playjava.handler;

import java.util.UUID;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

public final class UuidFactory {
    
    // UUIDv7を生成するためのGeneratorインスタンスの生成
    private static final TimeBasedGenerator GENERATOR = 
        Generators.timeBasedGenerator();
    
    // コンストラクタ
    private UuidFactory() {}

    // UUID生成
    public static UUID newUuid() {
        return GENERATOR.generate();
    }
}
