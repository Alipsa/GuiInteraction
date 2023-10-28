import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import javax.script.ScriptException;

public class ScriptRunner {

  public static void main(String[] args) throws IOException, ScriptException {
    // TODO figure out how to define the script path in the jar manifest
    var scriptPath = args[0];
    InOut io = new InOut();
    GroovyScriptEngineImpl engine = new GroovyScriptEngineImpl();
    engine.put("io", io);
    String jsonExporter = readContent(scriptPath);
    engine.eval(jsonExporter);
  }

  static String readContent(String resource) throws IOException {
    StringBuilder contentBuilder = new StringBuilder();
    try (InputStream is = ScriptRunner.class.getResourceAsStream(resource);
        Stream<String> stream = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines()) {
      stream.forEach(s -> contentBuilder.append(s).append('\n'));
    }
    return contentBuilder.toString();
  }
}
