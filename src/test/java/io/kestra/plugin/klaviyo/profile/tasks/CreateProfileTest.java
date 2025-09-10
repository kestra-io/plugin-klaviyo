//package io.kestra.plugin.klaviyo.profile.tasks;
//
//import io.kestra.core.junit.annotations.KestraTest;
//import io.kestra.core.runners.RunContext;
//import io.kestra.core.runners.RunContextFactory;
//import io.kestra.plugin.klaviyo.profile.models.Location;
//import io.kestra.plugin.klaviyo.tasks.CreateProfile;
//import jakarta.inject.Inject;
//import org.junit.jupiter.api.Test;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@KestraTest
//class CreateProfileTest {
//    @Inject
//    private RunContextFactory runContextFactory;
//
//    @Test
//    void run() throws Exception {
//        RunContext runContext = runContextFactory.of();
//
//        Location location = Location.builder()
//            .address1("123 MG Road")
//            .city("Bengaluru")
//            .country("IN")
//            .region("KA")
//            .zip("560038")
//            .timezone("Asia/Kolkata")
//            .build();
//
//        Map<String, Object> properties = new HashMap<>();
//        properties.put("plan", "premium");
//        properties.put("source", "website");
//
//        CreateProfile createProfile = CreateProfile.builder()
//            .privateApiKey(io.kestra.core.models.property.Property.of("pk_6ea0af6ad38547719ba334b97501419eb9"))
//            .email(io.kestra.core.models.property.Property.of("user@example.com"))
//            .phoneNumber(io.kestra.core.models.property.Property.of("+919999999999"))
//            .externalId(io.kestra.core.models.property.Property.of("cust-001"))
//            .firstName(io.kestra.core.models.property.Property.of("John"))
//            .lastName(io.kestra.core.models.property.Property.of("Doe"))
//            .organization(io.kestra.core.models.property.Property.of("Acme Inc."))
//            .locale(io.kestra.core.models.property.Property.of("en-US"))
//            .title(io.kestra.core.models.property.Property.of("Engineering Manager"))
//            .image(io.kestra.core.models.property.Property.of("https://cdn.example.com/u/john.jpg"))
//            .location(io.kestra.core.models.property.Property.of(location))
//            .properties(io.kestra.core.models.property.Property.of(properties))
//            .build();
//
//        CreateProfile.Output output = createProfile.run(runContext);
//        assertNotNull(output);
//    }
//}