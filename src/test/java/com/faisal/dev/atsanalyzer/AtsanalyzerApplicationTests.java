package com.faisal.dev.atsanalyzer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:atsanalyzer;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.jpa.open-in-view=false",
        "app.storage.upload-dir=${java.io.tmpdir}/atsanalyzer-test-uploads",
        "app.storage.max-file-size-bytes=5242880",
        "app.storage.allowed-extensions[0]=pdf",
        "app.storage.allowed-extensions[1]=docx",
        "app.storage.allowed-content-types[0]=application/pdf",
        "app.storage.allowed-content-types[1]=application/vnd.openxmlformats-officedocument.wordprocessingml.document"
})
class AtsanalyzerApplicationTests {

	@Test
	void contextLoads() {
	}

}
