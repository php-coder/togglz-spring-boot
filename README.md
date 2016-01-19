## Togglz Spring Boot Starter

## Getting Started

Add the Togglz Spring Boot dependency to your project:

    compile("com.github.marceloverdijk:togglz-spring-boot-starter")

Adding the `togglz-spring-boot-starter` dependency will add automatically `togglz-code` and `togglz-spring-code` modules
to your project.

Optionally add the Tooglz Admin Console, Togglz Spring Security and Togglz JUnit Testing Support dependencies:

    compile("org.togglz:togglz-console:${togglzVersion}")
    compile("org.togglz:togglz-spring-security:${togglzVersion}")
    testCompile("org.togglz:togglz-junit:${togglzVersion}")

## Auto Configuration

The `togglz-spring-boot-starter` will trigger the Togglz Spring Boot auto configuration.
It will create all necessary beans and in particular the Togglz `FeatureManager`.

The only thing that needs to provided is a `FeaturesProvider` e.g.:

	public enum MyFeatures implements Feature {

	    @EnabledByDefault
	    @Label("Hello World Feature")
	    HELLO_WORLD,

	    @Label("Reverse Greeting Feature")
	    REVERSE_GREETING;

	    public boolean isActive() {
	        return FeatureContext.getFeatureManager().isActive(this);
	    }
	}

    @Bean
    public FeatureProvider featureProvider() {
        return new EnumBasedFeatureProvider(MyFeatures.class);
    }

By default the auto configuration will use a `NoOpUserProvider` and when Spring Security is used a
`SpringSecurityUserProvider`.

If the `togglz-console` dependency is added then the `TogglzConsoleServlet` is registered using a
`ServletRegistrationBean`.

Also any beans of type `ActivationStrategy` found in the application content will be added to the `FeatureManager`
automatically.

If you define a `UserProvider`, `StateRepository`, `ActivationStrategyProvider` or even a `FeatureManager` manually the
auto configuration will not create these beans and will use the ones already available in the application context.

The auto configuration also support providing feature values inside your application.properties/application.yml. See an
example below of the application.yml. Note this is an Spring Boot in-memory implementation of a `StateRepository` which
does not persist edited features. If persistence is needed `FileBasedStateRepository`, `JDBCStateRepository` or any
other applicable `StateRepository` can be added to the application context manually. When you want to use a
`FileBasedStateRepository` you can also use this by providing a `togglz.features-file` application property.
If the `togglz.features-file` is provided this will take precedence over features provided via application properties
and effectively ignoring `togglz.features`.

## Application Properties

The following properties can be specified inside your application.properties/application.yml file or as command line switches:

	togglz:
	  enabled: true # Enable Togglz for the application.
	  console:
	    enabled: true # Enable admin console.
	    path: /togglz # The path of the admin console when enabled.
	  feature-manager-name: # The name of the feature manager
	  features: # The feature states
	    HELLO_WORLD: true
	    REVERSE_GREETING: true
	    REVERSE_GREETING.strategy: username
	    REVERSE_GREETING.param.users: user2, user3
	  features-file: # The path to the features file that contains the feature states.
	  security:
	    feature-admin-authority: ROLE_ADMIN # The name of the authority that is allowed to access the admin console.

## Samples

### Hello World

The Hello World sample is a sample using basic auto configuration.

Run `./gradlew clean :togglz-spring-boot-sample-hello-world:bootRun`

The sample project also contains `MockMvc` integration tests.

### Spring Security

The Spring Security sample demonstrates enabling/disabling features based on various users.

Run `./gradlew clean :togglz-spring-boot-sample-spring-security:bootRun`

The sample project also contains `MockMvc` integration tests.

## Building from Source

### Check out sources

`git clone git@github.com:marceloverdijk/togglz-spring-boot.git`

### Install starter jar into your local Maven cache

`./gradlew install`

## License

Code is released under version 2.0 of the [Apache License][].

[Apache License]: http://www.apache.org/licenses/LICENSE-2.0
