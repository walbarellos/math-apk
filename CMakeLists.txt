cmake_minimum_required(VERSION 3.22.1)
project("basecalc")

# C++17 para usar std::string, structured bindings, etc.
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Flags de otimização e warnings
add_compile_options(-O2 -Wall -Wextra)

add_library(
    basecalc
    SHARED
    BaseConverter.cpp
    jni_bridge.cpp
)

# Link com log do Android (útil para __android_log_print em debug)
find_library(log-lib log)
target_link_libraries(basecalc ${log-lib})
