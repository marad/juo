package io.github.marad.juo.mul

import io.github.marad.juo.mul.internal.MulFacadeImpl

class MulConfig {

    fun createMul(uoPath: String): MulFacade {
        return MulFacadeImpl(uoPath).also { it.load() }
    }

}