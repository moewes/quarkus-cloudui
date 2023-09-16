package net.moewes.cloudui.it;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class BeanTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/")
                .then()
                .statusCode(200)
                .body(containsString("<cloudui-view backend=\"/net.moewes.cloudui.it.MyView\""));
    }

    @Test
    public void testHelloEndpoint2() {
        given()
                .when().get("/net.moewes.cloudui.it.MyView")
                .then()
                .statusCode(200)
                .body(containsString("text from Bean"));
    }
}

