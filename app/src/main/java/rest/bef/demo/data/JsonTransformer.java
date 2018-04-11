package rest.bef.demo.data;

import com.google.gson.Gson;
import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;
import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {

    private static final Gson mapper = new Gson();

    @Override
    public String render(Object model) {
        return mapper.toJson(model);
    }
}
