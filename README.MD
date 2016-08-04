This is an example project of how to set up a library for quick work 
with Redis servers.

Modify it up for servers you currently work with, use sbt publishLocal,
and then you can add

"com.turdwaffle" %% "redislib" % "1.0"

as a dependency in your (local sbt) projects build.sbt file and start 
hget-ing!.