
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import storageoperations.*;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public static OcupacaoTemporaria convertLineToObject(String line) throws ParseException {
    String[] cols = line.split(",");
    OcupacaoTemporaria ocup = new OcupacaoTemporaria();
    ocup.ID = Integer.parseInt(cols[0]);
    ocup.location = new Localizacao();
    ocup.location.point = new GeoPoint(Double.parseDouble(cols[1]), Double.parseDouble(cols[2]));
    ocup.location.coord = new Coordenadas();
    ocup.location.coord.X = Double.parseDouble(cols[1]);
    ocup.location.coord.Y = Double.parseDouble(cols[2]);
    ocup.location.freguesia = cols[3];
    ocup.location.local = cols[4];
    ocup.event = new Evento();
    ocup.event.evtID = Integer.parseInt(cols[5]);
    ocup.event.nome = cols[6];
    ocup.event.tipo = cols[7];
    ocup.event.details = new HashMap<String, String>();
    if (!cols[8].isEmpty()) ocup.event.details.put("Participantes", cols[8]);
    if (!cols[9].isEmpty()) ocup.event.details.put("Custo", cols[9]);
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    ocup.event.dtInicio = formatter.parse(cols[10]);
    ocup.event.dtFinal = formatter.parse(cols[11]);
    ocup.event.licenciamento = new Licenciamento();
    ocup.event.licenciamento.code = cols[12];
    ocup.event.licenciamento.dtLicenc = formatter.parse(cols[13]);
    return ocup;
}

public static void insertDocuments(String pathnameCSV, Firestore db, String collectionName)
        throws Exception {
    BufferedReader reader = new BufferedReader(new FileReader(pathnameCSV));
    CollectionReference colRef = db.collection(collectionName);
    String line;
    while ((line = reader.readLine()) != null) {
        OcupacaoTemporaria ocup = convertLineToObject(line);
        DocumentReference docRef = colRef.document("Lab4-" + ocup.ID);
        ApiFuture<WriteResult> resultFut = docRef.set(ocup);
        WriteResult result = resultFut.get();
        System.out.println("Update time : " + result.getUpdateTime());
    }
}

/**
 * Adicione à aplicação as seguintes funcionalidades:
 * a. Apresentar o conteúdo de um documento a partir do seu identificador (ex: Lab4-2017).
 * b. Apagar um campo de um documento, dado o seu identificador e o nome do campo a
 * eliminar.
 * c. Realizar uma interrogação simples para obter todos os documentos de uma determinada
 * freguesia.
 * d. Realizar uma interrogação composta para obter os documentos com os seguintes critérios:
 * ■ Com o campo ID maior que um valor
 * ■ De uma determinada freguesia
 * ■ De um determinado tipo de evento
 * e. Realizar uma interrogação para obter os documentos com eventos que iniciaram no mês
 * de fevereiro de 2017 (data de início (dtInicio) maior que 31/01/2017 e menor que 01/03/2017).
 * f.Realizar uma interrogação para obter os documentos com eventos integralmente
 * realizados no mês de fevereiro de 2017 (data de início (dtInicio) maior que 31/01/2017
 * e data final (dtFinal) menor que 01/03/2017)
 */
public void main() throws Exception {
    InputStream serviceAccount = new FileInputStream(KEY_JSON);
    GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
    FirestoreOptions options = FirestoreOptions
            .newBuilder().setDatabaseId("db-name").setCredentials(credentials)
            .build();
    Firestore db = options.getService();
    //insertDocuments("src/main/resources/OcupacaoEspacosPublicos.csv", db, "OcupacaoEspacosPublicos");
    CollectionReference cref = db.collection("OcupacaoEspacosPublicos");
    //a
    DocumentReference docRefRead = cref.document("Lab4-2017");
    ApiFuture<DocumentSnapshot> future = docRefRead.get();
    DocumentSnapshot document = future.get();
    if (document.exists()) {
        System.out.println("Document data: " + document.getData());
    } else {
        System.out.println("No such document!");
    }

//    //b
//    DocumentReference docRefDelete = db.document("OcupacaoEspacosPublicos/Lab4-2017");
//    //apagar campo "location"
//    Map<String, Object> updates = new HashMap<>();
//    updates.put("location.coord", FieldValue.delete());
//    ApiFuture<WriteResult> writeResult = docRefDelete.update(updates);
//    System.out.println("Update time : " + writeResult.get());

    //c
    FieldPath fp = FieldPath.of("location", "freguesia");
    Query query = db.collection("OcupacaoEspacosPublicos").whereEqualTo(fp, "Alvalade");
    ApiFuture<QuerySnapshot> querySnapshot = query.get();
    for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
        System.out.print("DocID: " + doc.getId() + " @ " + doc.get("location"));
    }

    System.out.println("\n Query c completed");
    //d
    FieldPath fpathLocation = FieldPath.of("location", "freguesia");
    FieldPath fpatchEvent = FieldPath.of("event", "tipo");
    Query query2 = db.collection("OcupacaoEspacosPublicos")
            .whereGreaterThan("ID", 2017)
            .whereEqualTo(fpathLocation, "Alvalade")
            .whereEqualTo(fpatchEvent, "Filmagem");
    ApiFuture<QuerySnapshot> querySnapshot2 = query2.get();
    for (DocumentSnapshot doc: querySnapshot2.get().getDocuments()) {
        System.out.println(doc.getId()+":Doc:"+doc.getData());
    }

    System.out.println("\n Query d completed");
    //e
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    Timestamp start = Timestamp.of(formatter.parse("31/01/2017"));
    Timestamp end = Timestamp.of(formatter.parse("01/03/2017"));
    Query query3 = db.collection("OcupacaoEspacosPublicos")
            .whereGreaterThan("event.dtInicio", start)
            .whereLessThan("event.dtFinal", end);
    ApiFuture<QuerySnapshot> querySnapshot3 = query3.get();
    for (DocumentSnapshot doc: querySnapshot3.get().getDocuments()) {
        System.out.println(doc.getId()+":Doc:"+doc.getData());
    }

    System.out.println("\n Query e completed");
    //f
    Query query4 = db.collection("OcupacaoEspacosPublicos")
            .whereGreaterThan("event.dtInicio", start)
            .whereLessThan("event.dtFinal", end);
    ApiFuture<QuerySnapshot> querySnapshot4 = query4.get();
    for (DocumentSnapshot doc: querySnapshot4.get().getDocuments()) {
        System.out.println(doc.getId()+":Doc:"+doc.getData());
    }

}

private static final String KEY_JSON = "cn2324-t1-g09-999ae8265231.json";