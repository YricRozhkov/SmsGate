package tech.bogomolov.incomingsmsgateway;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class ForwardingConfig {
    final private Context context;
    private String sender;
    private String url;
    private String variables;
    public String getVars(){ return this.variables; }
    public void setVars(String Vars){this.variables=Vars;}
    public ForwardingConfig(Context context) {
        this.context = context;
    }

    public String getSender() {
        return this.sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void save() {
        SharedPreferences.Editor editor = getEditor(context);
        try {
            JSONObject json = new JSONObject();
            json.put("url", this.url);
            json.put("vars", this.variables);
            editor.putString(this.sender, json.toString());
//        editor.putString(this.sender, this.url);
            editor.commit();
        }catch(JSONException err){
            Log.d("Error", err.toString());
        }
    }

    public static ArrayList<ForwardingConfig> getAll(Context context) {
        SharedPreferences sharedPref = getPreference(context);
        Map<String, ?> sharedPrefs = sharedPref.getAll();

        ArrayList<ForwardingConfig> configs = new ArrayList<ForwardingConfig>();
        for (Map.Entry<String, ?> entry : sharedPrefs.entrySet()) {
            try {
                JSONObject jsonObject = new JSONObject((String) entry.getValue());
                ForwardingConfig config = new ForwardingConfig(context);
                config.setSender(entry.getKey());
                config.setUrl ((String) jsonObject.getString("url") );
                config.setVars((String) jsonObject.getString("vars") );
//            config.setUrl((String) entry.getValue());
                configs.add(config);
            }catch (JSONException err){
                Log.d("Error", err.toString());
            }
        }

        return configs;
    }

    public void remove() {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(this.getSender());
        editor.commit();
    }

    private static SharedPreferences getPreference(Context context) {
        return context.getSharedPreferences(
                context.getString(R.string.key_phones_preference),
                Context.MODE_PRIVATE
        );
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences sharedPref = getPreference(context);
        return sharedPref.edit();
    }
}
