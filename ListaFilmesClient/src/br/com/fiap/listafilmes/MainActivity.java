package br.com.fiap.listafilmes;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MainActivity extends Activity {
	
	private ListView filmesListView;
	private RadioGroup rgGenero;
	private List<String> listaFilmes;
	private ArrayAdapter<String> adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		filmesListView = (ListView) findViewById(R.id.filmesLV);
		rgGenero = (RadioGroup) findViewById(R.id.rgCategoria);
		rgGenero.setOnCheckedChangeListener(changeListener);
		
		listaFilmes = new ArrayList<String>();
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaFilmes);
		
		try {
			ClienteHTTP client = new ClienteHTTP();
			client.execute("terror"); //executa inicialmente com filmes de TERROR
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String request(String genero) throws Exception {
		StringBuilder retorno = new StringBuilder();
		
		HttpClient client = new DefaultHttpClient();
		String url = "http://10.0.2.2:8080/ListaFilmesServer/ListaFilmeServlet?genero=" + genero;
		HttpGet get = new HttpGet(url);
		
		HttpResponse response = client.execute(get);
		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		
		if (statusCode == 200) {
			HttpEntity entity = response.getEntity();
			InputStream content = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			String line;
			while ((line = reader.readLine()) != null) {
				retorno.append(line);
			}
			
			return retorno.toString();
			
		} else {
			return "ERRO";
		}
	}
	
	private class ClienteHTTP extends AsyncTask<String, Void, String> {
		
		@Override
		protected String doInBackground(String... params) {
			String ret = "ERRO";
			try {
				ret = request(params[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return ret;
		}
		
		@Override
		protected void onPostExecute(String result) {
			try {
				listaFilmes.clear();
				System.out.println(result);
				JSONObject obj = new JSONObject(result);
				JSONArray filmes = (JSONArray) obj.get("filmes");
				for (int i = 0; i < filmes.length(); i++) {
					JSONObject f = (JSONObject) filmes.get(i);
					listaFilmes.add(f.getString("titulo"));
					System.out.println(f.getString("titulo"));
					
					filmesListView.invalidateViews();
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private OnCheckedChangeListener changeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			String genero = "";
			
			if (checkedId == R.id.radioAcao) {
				genero = "acao";
			} else {
				genero = "terror";
			}
			
			ClienteHTTP client = new ClienteHTTP();
			client.execute(genero);
		}
	};
}
