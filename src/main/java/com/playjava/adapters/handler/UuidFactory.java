package com.playjava.adapters.handler;

import java.util.UUID;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;

public final class UuidFactory {
    
    // UUIDv7を生成するためのGeneratorインスタンスの生成
    private static final TimeBasedEpochGenerator GENERATOR = 
        Generators.timeBasedEpochGenerator();
    
    // コンストラクタ
    private UuidFactory() {}

    // UUID生成
    public static UUID newUuid() {
        return GENERATOR.generate();
    }
}
