//assembly configuration for generating uber jar

assembly / mainClass := Some("zendesk.rummage.Rummage")
assembly / assemblyOutputPath := new java.io.File("target/rummage.jar")
