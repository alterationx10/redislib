This is an example project of how to set up a library for quick work 
with Redis servers.

Modify it up for servers you currently work with, use sbt publishLocal,
and then you can add

"com.turdwaffle" %% "redislib" % "1.0"

(insert correct version)

as a dependency in your (local sbt) projects build.sbt file and start 
hget-ing!.

Also, since it's set up via Guice, you can enable it in a Play! app,
and inject it right into your Controllers/etc...