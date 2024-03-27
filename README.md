# webMethods Application Manager (wam)

Source code for cli tool to generate a Dockerfile to build a webMethods runtime image from a project scaffold(s) or edge manifest file

### Run tests

```
mvn test
```

### Generate jar file

```
mvn install
```

> *NOTE* Excludes libraries, you will need to bundle the following libraries
> commons-cli.1.6.0
> org.yaml-snakeYaml.2.0

-----
These tools are provided as-is and without warranty or support. They do not constitute part of the Software AG product suite. Users are free to use, fork and modify them, subject to the license agreement. While Software AG welcomes contributions, we cannot guarantee to include every contribution in the master project.
