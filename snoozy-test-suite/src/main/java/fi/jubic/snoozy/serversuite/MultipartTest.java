package fi.jubic.snoozy.serversuite;

import fi.jubic.snoozy.Application;
import fi.jubic.snoozy.Server;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.jupiter.api.Test;

import javax.annotation.security.PermitAll;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.jubic.snoozy.test.TestUtil.withServer;
import static org.junit.jupiter.api.Assertions.assertEquals;

public interface MultipartTest<T extends Server> extends BaseTest<T> {
    @Test
    default void partCollectionInjectedFromContext() throws Exception {
        withServer(
                instance(),
                new MultipartApplication(),
                (hostname, port) -> {
                    OkHttpClient client = new OkHttpClient();

                    MultipartBody body = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("title", "TEST")
                            .addFormDataPart(
                                    "file",
                                    "test.csv",
                                    RequestBody.create(
                                            okhttp3.MediaType.parse("text/csv"),
                                            new File(
                                                    getClass().getClassLoader()
                                                            .getResource("test.csv")
                                                            .getFile()
                                            )
                                    )
                            )
                            .build();

                    okhttp3.Response response = client.newCall(
                            new Request.Builder()
                                    .url(String.format("http://%s:%d/multipart", hostname, port))
                                    .post(body)
                                    .build()
                    ).execute();



                    assertEquals(200, response.code());
                    System.out.println(response.body().toString());
                }
        );
    }

    class MultipartApplication extends Application {
        @Override
        public Set<Object> getSingletons() {
            return Collections.singleton(new MultipartResource());
        }
    }

    @Path("multipart")
    @Produces(MediaType.TEXT_PLAIN)
    class MultipartResource {
        @POST
        @PermitAll
        public Map<String, String> postParts(@Context HttpServletRequest request) throws IOException, ServletException {
            return request.getParts().stream()
                    .collect(Collectors.toMap(
                            Part::getName,
                            part -> {
                                try {
                                    return new BufferedReader(new InputStreamReader(part.getInputStream()))
                                            .lines().collect(Collectors.joining("\n"));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    ));
        }
    }
}
