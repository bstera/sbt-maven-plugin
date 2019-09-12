### How to use ?
-  Add plugin dependency to your pom.
```$xml
    <build>
		<plugins>
			<plugin>
				<groupId>com.bstera.plugin</groupId>
				<artifactId>sbt-maven-plugin</artifactId>
				<version>1.0.0</version>
				<dependencies>
					<dependency>
						<groupId>org.codehaus.groovy</groupId>
						<artifactId>groovy-all</artifactId>
						<!-- any version of Groovy \>= 1.5.0 should work here -->
						<version>2.5.7</version>
						<scope>runtime</scope>
						<type>pom</type>
					</dependency>
				</dependencies>
			</plugin>
		</plugins>
	</build>
```
- Run **mvn sbt:info** to get support commands