cmake_minimum_required(VERSION 3.22.1)

project(smartphone_roleplay_native)

add_library(
    smartphoneroleplay_native
    SHARED
    native_runtime.cpp
)

find_library(log-lib log)

 target_link_libraries(
    smartphoneroleplay_native
    ${log-lib}
)
