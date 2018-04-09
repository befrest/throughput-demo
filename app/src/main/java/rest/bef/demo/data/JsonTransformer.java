package rest.bef.demo.data;

import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;
import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {

    private static final ObjectMapper PARSER = JsonFactory.create();

    @Override
    public String render(Object model) throws Exception {
        return PARSER.toJson(model);
/*        String result;

        JsonSerializer ackParser = new JsonSerializerFactory()
                .addPropertySerializer((jsonSerializerInternal, instance, fieldAccess, builder) -> {

                    String field = fieldAccess.getField().getName();
                    if (field.equals("entity")) {
                        Object entity = ((AckDTO) instance).getEntity();
                        builder.addJsonFieldName(field);
                        builder.add(PARSER.toJson(entity));
                        return true;
                    }
                    return false;
                }).create();

        if (model instanceof AckDTO) {
            if (((AckDTO) model).getErrorCode() == null)
                ((AckDTO) model).setErrorCode(Constants.System.OKAY);
            result = String.valueOf(ackParser.serialize(model));
        } else
            result = PARSER.writeValueAsString(model);

        return result;*/
    }
}
