package adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import models.Funko;

import java.lang.reflect.Type;

public class LocalDateAdapter implements JsonSerializer<Funko> {
    /**
     * Serializa un objeto `Weather` en formato JSON.
     *
     * @param weatherType  El objeto `Weather` que se va a serializar.
     * @param typeOfSrc    El tipo de objeto de origen (puede ser útil en la serialización).
     * @param context      El contexto de serialización que se utiliza para realizar la serialización.
     * @return Un elemento JSON que representa el objeto `Weather`.
     */
    @Override
    public JsonElement serialize(Funko funko, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("cod", funko.getCod().toString());
        jsonObject.addProperty("id2", funko.getId2());
        jsonObject.addProperty("nombre", funko.getNombre());
        jsonObject.addProperty("modelo", funko.getModelo().toString());
        jsonObject.addProperty("precio", funko.getPrecio());
        jsonObject.addProperty("fechaLanzamiento", funko.getFechaLanzamiento().toString());
        return jsonObject;
    }
}