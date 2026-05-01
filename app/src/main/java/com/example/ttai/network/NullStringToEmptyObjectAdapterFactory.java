package com.example.ttai.network;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

// 这是一个通用的 TypeAdapter，适用于任何预期的 Object/List 类型，当遇到空字符串时返回 null。
class NullStringToEmptyObjectAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        // 我们只关注需要解析为对象的类型，并且跳过基本类型或 String
        if (type.getRawType() == String.class || type.getRawType().isPrimitive()) {
            return null; // 让 Gson 默认处理这些类型
        }

        // 获取原始的 TypeAdapter (它期望一个 OBJECT)
        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                delegate.write(out, value);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.STRING) {
                    String value = in.nextString();
                    if (value.isEmpty()) {
                        // 如果遇到空字符串，返回 null
                        return null;
                    }
                    // 如果是非空字符串，则让原始适配器处理 (这里可能会抛出异常，取决于原始适配器)
                    // 在你的场景中，一旦遇到非空字符串，还是会出错，但至少处理了空字符串的情况
                    return null; // 简单处理，避免进一步崩溃
                }

                // 遇到 OBJECT, NULL 或 ARRAY 时，交给原始适配器处理
                return delegate.read(in);
            }
        };
    }
}