# umad - Unsynchronized Memory Access Detector

A Java agent that rewrites bytecode to instrument calls to methods which might be used by different threads and check at runtime if no such access happened.