# APP Komertziala - Komertzialen Agenda eta Eskaeren Kudeaketa

## 📋 Proiektuaren Sarrera

**APP Komertziala** Android aplikazio profesional bat da, komertzialen eguneroko lan-fluxua errazteko diseinatua. Aplikazioak bi funtzionalitate nagusi eskaintzen ditu:

- **Agenda Kudeaketa**: Komertzial bakoitzak bere bisitak eta zita-programazioak kudeatu ditzake, bazkideekin izandako harremanak jarraituz.
- **Eskaeren Kudeaketa**: Eskaera berriak sortu, editatu eta jarraitu, katalogoko produktuekin lotuta.

Aplikazioak **XML fitxategien bidezko sinkronizazioa** onartzen du, datu-base lokal bat mantentzen du Room Persistence Library erabiliz, eta **segurtasun iragazki zorrotza** inplementatzen du: komertzial bakoitzak bere datuak bakarrik ikus ditzake.

---

## 🏗️ Arkitektura

### MVVM eta Repository Patroiak

Aplikazioak **MVVM (Model-View-ViewModel)** arkitektura eta **Repository** patroia erabiltzen ditu, geruzen arteko banaketa garbia bermatzeko:

```
┌─────────────────────────────────────────┐
│           UI Layer (Activities)        │
│  - LoginActivity                        │
│  - MainActivity                         │
│  - AgendaModuluaActivity                │
│  - EskaerakActivity                     │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Repository Layer                   │
│  - AgendaRepository                     │
│  - DatuKudeatzailea                     │
│  - XMLKudeatzailea                      │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Data Layer (Room)                  │
│  - AppDatabase                          │
│  - DAO Interfaces                       │
│  - Entity Classes                       │
└─────────────────────────────────────────┘
```

**Abantailak:**
- **Testagarritasuna**: Logika UI-tik bananduta dago, unitate-probak errazagoak dira.
- **Mantentze-erraztasuna**: Geruza bakoitzak bere erantzukizuna du.
- **Errendimendua**: Repository-ek `ExecutorService` erabiltzen du datu-base eragiketak hari nagusitik kanpo exekutatzeko.

---

## 🔑 Klase Nagusiak

### LoginActivity

**Helburua**: Saioa hasiera eta segurtasun geruza.

**Funtzionalitateak:**
- XML fitxategien inportazioa (assets-etik edo gailutik)
- Komertzial hautaketa (datu-basean oinarrituta)
- Erabiltzaile/pasahitza balidazioa (`Logina` taularen arabera)
- Google Maps integrazioa (Donostia zentratua)
- `SessionManager` erabiliz saio-kudeaketa segurua

**Kode adibidea:**
```java
// SEGURTASUNA: SessionManager erabiliz saioa hasi
SessionManager sessionManager = new SessionManager(this);
sessionManager.saioaHasi(komertziala.getKodea(), komertziala.getIzena());
```

### Bazkidea Entitatea

Bazkideen datu-egitura, `bazkideak.xml` fitxategiaren egiturari dagokiona.

**Eremuak:**
- `id` (Primary Key, auto-generatua)
- `nan` (NAN identifikatzailea, indizea)
- `izena`, `abizena`
- `telefonoZenbakia`, `posta`
- `jaiotzeData` (yyyy/MM/dd formatua)
- `argazkia` (argazki fitxategiaren izena)
- `kodea`, `helbidea`, `probintzia`
- `komertzialKodea` (Foreign Key → `komertzialak.kodea`, `ON DELETE CASCADE`)
- `sortutakoData`

**Indizeak:**
- `nan` (bilaketa azkartzeko)
- `izena` (bilaketa azkartzeko)
- `kodea`
- `komertzialKodea` (Foreign Key erlazioa)

### Eskaera Entitateak

Eskaeren kudeaketa hiru entitate desberdinen bidez egiten da:

#### EskaeraGoiburua
Eskaeraren goiburua: zenbakia, data, komertzial kodea/ID, ordezkaritza, bazkidea kodea/ID.

**Gako nagusia**: `zenbakia` (String, Primary Key)

**Indizeak:**
- `komertzialKodea`, `bazkideaKodea`
- `komertzialId`, `bazkideaId`

#### EskaeraXehetasuna
Eskaeraren xehetasunak: artikulu kodea, kantitatea, prezioa.

**Gako nagusia**: `id` (auto-generatua)

**Indizeak:**
- `eskaeraZenbakia` (EskaeraGoiburua-rekin lotura)
- `artikuluKodea` (Katalogoa-rekin lotura)

#### Eskaera (Zaharra)
XML-etik datozen eskaera zaharren formatua (`bazkideak.xml`-eko `<eskaerak>` blokea).

**Eremuak:**
- `eskaeraID`, `prodIzena`, `data`, `kopurua`, `prodArgazkia`
- `bazkideaId` (Foreign Key → `bazkideak.id`, `ON DELETE CASCADE`)

---

## 💾 Datu-basea eta Room

### Room Persistence Library

Aplikazioak **Room Persistence Library 2.6.1** erabiltzen du SQLite datu-base lokal bat kudeatzeko. Room-ek konpilazio garaian SQL kontsultak baliozkotzen ditu eta type-safe DAO interfazeak sortzen ditu.

### Entitate Taulak

| Taula | Gako Nagusia | Deskribapena |
|-------|--------------|--------------|
| `komertzialak` | `id` (auto) | Komertzialen informazioa |
| `bazkideak` | `id` (auto) | Bazkideen datu osoak |
| `katalogoa` | `artikuluKodea` | Produktuen katalogoa |
| `eskaera_goiburuak` | `zenbakia` | Eskaeren goiburuak |
| `eskaera_xehetasunak` | `id` (auto) | Eskaeren xehetasunak |
| `eskaerak` | `id` (auto) | Eskaera zaharrak (XML formatua) |
| `agenda_bisitak` | `id` (auto) | Bisiten agenda |
| `loginak` | `id` (auto) | Erabiltzaile/pasahitza sarbideak |

### Taulen Arteko Erlazioak (@ForeignKey)

#### Bazkidea → Komertziala
```java
@ForeignKey(
    entity = Komertziala.class,
    parentColumns = "kodea",
    childColumns = "komertzialKodea",
    onDelete = ForeignKey.CASCADE
)
```
**Eragina**: Komertzial bat ezabatzen denean, bere bazkideak automatikoki ezabatzen dira.

#### Eskaera → Bazkidea
```java
@ForeignKey(
    entity = Bazkidea.class,
    parentColumns = "id",
    childColumns = "bazkideaId",
    onDelete = ForeignKey.CASCADE
)
```
**Eragina**: Bazkide bat ezabatzen denean, bere eskaerak automatikoki ezabatzen dira.

### Indizeak (indices)

Indizeak kontsultak azkartzeko erabiltzen dira:

**Bazkideak taula:**
- `nan`: NAN bidezko bilaketa azkarra
- `izena`: Izen bidezko bilaketa
- `kodea`: Kode bidezko bilaketa
- `komertzialKodea`: Foreign Key erlazioa azkartzeko

**Agenda taula:**
- `bazkideaKodea`, `bisitaData`, `bazkideaId`, `komertzialaId`, `komertzialKodea`
- Indize konposatua: `(komertzialKodea, bazkideaKodea, bisitaData)`

**EskaeraGoiburua taula:**
- `komertzialKodea`, `bazkideaKodea`, `komertzialId`, `bazkideaId`

---

## 🔄 Migrazioak

### Bertsio Kudeaketa

Datu-baseak **15 bertsio** ditu, eskema aldaketak migrazio estrategia baten bidez kudeatzen direlarik.

### Migrazio Estrategia

**Bertsio igoerak:**
- Migrazio bakoitza `Migration` klase baten bidez definitzen da
- `taulaExistitzenDa()` eta `zutabeaExistitzenDa()` metodoak bikoiztuak saihesteko erabiltzen dira
- Transakzio seguruak: `runInTransaction()` erabiliz

**Adibidea (MIGRAZIO_13_14):**
```java
private static final Migration MIGRAZIO_13_14 = new Migration(13, 14) {
    @Override
    public void migrate(SupportSQLiteDatabase db) {
        // Table swap estrategia: Foreign Key gehitzeko
        // 1. Sortu taula berria egitura ZUZEKIN
        db.execSQL("CREATE TABLE bazkideak_new (...)");
        // 2. Kopiatu datu guztiak
        db.execSQL("INSERT INTO bazkideak_new SELECT ... FROM bazkideak");
        // 3. Ezabatu taula zaharra eta aldatu izena
        db.execSQL("DROP TABLE bazkideak");
        db.execSQL("ALTER TABLE bazkideak_new RENAME TO bazkideak");
    }
};
```

### fallbackToDestructiveMigration Estrategia

```java
.fallbackToDestructiveMigration()
```

**Erabilera**: Eskema aldaketa handi bat gertatzen denean (adib. Foreign Key gehitzea taula zaharretan), datu-base zaharra ezabatu eta berria sortzen da. **OHARRA**: Produkzioan datu garrantzitsuak badaude, migrazio espezifikoak idatzi behar dira.

**Migrazio garrantzitsuenak:**
- **MIGRAZIO_2_3**: `agenda_bisitak` taula sortu
- **MIGRAZIO_4_5**: `bazkideak` taula sortu
- **MIGRAZIO_6_7**: `eskaera_goiburuak` eta `eskaera_xehetasunak` taulak berriz sortu
- **MIGRAZIO_7_8**: `komertzialId` eta `bazkideaId` eremuak gehitu
- **MIGRAZIO_13_14**: `bazkideak` taula Foreign Key-ekin berriz sortu (table swap)

---

## ⚙️ Funtzionalitate Kritikoak

### XML bidezko Sinkronizazioa eta "Upsert" Logika

Aplikazioak XML fitxategiak inportatzeko aukera ematen du (`komertzialak.xml`, `bazkideak.xml`, `katalogoa.xml`, `agenda.xml`, `loginak.xml`).

**Upsert Logika:**
- **Existitzen bada**: Eguneratu (`OnConflictStrategy.REPLACE`)
- **Existitzen ez bada**: Txertatu

**Inplementazioa:**
```java
// Bazkidea existitzen den egiaztatu
Bazkidea existitzenDa = bazkideaDao.nanBilatu(bazkidea.getNan().trim());

if (existitzenDa != null) {
    // Eguneratu: ID mantendu eta XML-etik datozen eremuak bakarrik eguneratu
    bazkidea.setId(existitzenDa.getId());
    bazkideaId = bazkideaDao.txertatu(bazkidea); // REPLACE estrategia
    // Ezabatu bazkidearen eskaera zaharrak
    eskaeraDao.ezabatuBazkidearenEskaerak(bazkideaId);
} else {
    // Bazkidea berria txertatu
    bazkideaId = bazkideaDao.txertatu(bazkidea);
}
```

**Transakzio Seguruak:**
```java
int emaitzaKopurua = db.runInTransaction(() -> {
    // Datu guztiak transakzio bakar batean gorde
    // Errore bat gertatzen bada, guztia atzera egingo da
});
```

### Segurtasun Iragazkia

**Helburua**: Komertzial bakoitzak bere datuak bakarrik ikusteko gaitasuna.

**Inplementazioa:**
1. **SessionManager**: Saioa hasi duen komertzialaren kodea gordetzen du
2. **DAO kontsultak**: `komertzialKodea` parametroa beti gehitzen da `WHERE` klausulan
3. **Repository geruza**: `SessionManager` erabiliz uneko komertzialaren kodea lortzen du

**Adibidea (AgendaRepository):**
```java
public void bilatuBezeroaz(@NonNull String filter, @NonNull KargatuCallback callback) {
    executorService.execute(() -> {
        // SEGURTASUNA: SessionManager erabiliz uneko komertzialaren kodea lortu
        SessionManager sessionManager = new SessionManager(context);
        String komertzialKodea = sessionManager.getKomertzialKodea();
        
        if (komertzialKodea == null || komertzialKodea.isEmpty()) {
            callback.onEmaitza(new ArrayList<>());
            return;
        }
        
        // SEGURTASUNA: bakarrik uneko komertzialaren bisitak
        List<Agenda> bisitak = agendaDao.bilatuBezeroaz(filter.trim(), komertzialKodea);
        callback.onEmaitza(bisitak);
    });
}
```

**DAO kontsulta (AgendaDao):**
```java
@Query("SELECT * FROM agenda_bisitak WHERE " +
       "(bazkideaKodea LIKE '%' || :filter || '%' OR " +
       "deskribapena LIKE '%' || :filter || '%') AND " +
       "komertzialKodea = :komertzialKodea " +
       "ORDER BY bisitaData DESC")
List<Agenda> bilatuBezeroaz(String filter, String komertzialKodea);
```

### Eskaerak Egin Aurreko Datuen Balidazio Zorrotza

Eskaera bat sortu aurretik, datuen balidazio zorrotza egiten da:

**UI Mailako Balidazioa (BisitaFormularioActivity):**
```java
private boolean baliozkotuFormularioa() {
    // Hutsen kontrolak
    if (data.isEmpty() || deskribapena.isEmpty() || bazkideaEzHautatua) {
        // Erabiltzaileari errore mezuak erakutsi
        return false;
    }
    
    // Formatuen egiaztapena
    if (!dataFormatuaZuzena(data)) {
        // Data YYYY-MM-DD formatuan izan behar du
        return false;
    }
    
    if (!ordua.isEmpty() && !ordua.matches("\\d{2}:\\d{2}")) {
        // Ordua HH:mm formatuan izan behar du
        return false;
    }
    
    return true;
}
```

**Datu-base Mailako Balidazioa:**
```java
// Kanpo-gakoen egiaztapena: bazkidea_kodea Bazkidea taulan existitzen dela ziurtatu
if (datuBasea.bazkideaDao().nanBilatu(bazkideaKodeaFinal) == null) {
    runOnUiThread(() -> Toast.makeText(this, "Bazkidea ez da existitzen", Toast.LENGTH_LONG).show());
    return;
}

// SEGURTASUNA: SessionManager erabiliz uneko komertzialaren kodea lortu
SessionManager sessionManager = new SessionManager(this);
String komertzialKodeaSegurua = sessionManager.getKomertzialKodea();

// Egiaztatu komertzial kodea bat datorrela
if (!komertzialKodeaSegurua.equals(komertzialKodeaFinal)) {
    runOnUiThread(() -> Toast.makeText(this, "Errorea: komertzial kodea ez dator bat", Toast.LENGTH_LONG).show());
    return;
}
```

**Transakzio Seguruak:**
```java
datuBasea.runInTransaction(() -> {
    // Datu guztiak transakzio bakar batean gorde
    // Errore bat gertatzen bada, guztia atzera egingo da
});
```

---

## 🛠️ Teknologia-Stacka

- **Hizkuntza**: Java 11
- **Android SDK**: MinSdk 24, TargetSdk 36, CompileSdk 36
- **Datu-basea**: Room Persistence Library 2.6.1
- **UI**: Material Design Components, ViewBinding
- **Mapak**: Google Maps API (Play Services Maps 18.2.0)
- **Arkitektura**: MVVM + Repository Pattern
- **Thread Management**: ExecutorService (4 thread pool)

---

## 📦 Instalazioa eta Konfigurazioa

### Beharrezkoak

1. **Android Studio** (Arctic Fox edo berriagoa)
- **JDK 11** edo berriagoa
- **Google Maps API gakoa** (`local.properties` fitxategian)

### Konfigurazioa

1. `local.properties` fitxategia sortu proiektuaren erroan:
```properties
MAPS_API_KEY=zure_google_maps_api_gakoa
```

2. XML fitxategiak `app/src/main/assets/` karpetan jarri:
   - `komertzialak.xml`
   - `bazkideak.xml`
   - `katalogoa.xml`
   - `agenda.xml`
   - `loginak.xml`

3. Proiektua eraiki:
```bash
./gradlew build
```

---

## 📝 Kode Estiloa

Proiektuak **euskara hutsa** erabiltzen du kodea idazteko:
- Klaseak, metodoak, aldagaiak: euskaraz
- Iruzkinak: teknikoak eta laburrak, beti euskaraz
- Nomenklatura: `camelCase` (adib. `lortuGuztiak()`, `unekoKomertziala`)

**Adibidea:**
```java
/**
 * Bisita bat txertatu (upsert: existitzen bada eguneratu, bestela sortu).
 * @param agenda Txertatu behar den bisita
 * @param callback Emaitza jaso behar duen callback
 */
public void txertatuBisita(@NonNull Agenda agenda, @Nullable KargatuCallback callback) {
    executorService.execute(() -> {
        try {
            long id = agendaDao.txertatu(agenda);
            if (callback != null) {
                callback.onEmaitza(id > 0 ? Collections.singletonList(agenda) : new ArrayList<>());
            }
        } catch (Exception e) {
            Log.e(ETIKETA, "Errorea bisita txertatzean", e);
            if (callback != null) callback.onEmaitza(null);
        }
    });
}
```

---

## 🔒 Segurtasuna

- **SessionManager**: Saio-kudeaketa segurua SharedPreferences erabiliz
- **Segurtasun iragazkia**: Komertzial bakoitzak bere datuak bakarrik ikusten ditu
- **Balidazio zorrotza**: Datuak datu-basean sartu aurretik baliozkotzen dira
- **Transakzio seguruak**: Datuen osotasuna bermatzeko

---

## 📄 Lizentzia

Proiektu hau pribatua da eta jabetza intelektualaren babesa du.

---

## 👥 Garatzaileak

**Techno Basque** - Android garapena eta kode garbia.

---

## 📁 Fitxategien Deskribapena

Proiektuko Java fitxategi guztien deskribapen tekniko eta funtzionala. Fitxategi bakoitzak helburu argi bat du eta proiektuaren osotasunean bere erantzukizuna betetzen du.

### Activities (Pantailak)

| Fitxategia | Helburua | Funtzio Garrantzitsuenak |
|------------|----------|--------------------------|
| **LoginActivity** | Saioa hasiera eta segurtasun geruza | `attemptLogin()`: Erabiltzaile/pasahitza balidazioa<br>`sartuKomertzialGisa()`: Komertzial hautaketa<br>`kargatuXmlGuztiak()`: XML fitxategiak inportatu<br>`onMapReady()`: Google Maps Donostian zentratu |
| **MainActivity** | Pantaila nagusia (BottomNavigationView) | `onCreate()`: Tab-ak konfiguratu (Hasiera, Agenda, Bazkideak, Inventarioa)<br>`kargatuKatalogoa()`: Produktuen zerrenda kargatu<br>`kargatuBazkideak()`: Bazkideen zerrenda kargatu<br>`gordeEskaera()`: Saskiko produktuak eskaera gisa gorde<br>`onMapReady()`: Google Maps + kontaktua (Map, Call, Email intents) |
| **AgendaModuluaActivity** | Agenda moduluaren pantaila nagusia | `kargatuZerrenda()`: Bisiten zerrenda kargatu (Repository erabiliz)<br>`konfiguratuBilaketa()`: Bilaketa funtzioa (data, bezeroa, deskribapena)<br>`esportatuEtaBidali()`: XML/TXT esportatu eta Gmail bidez bidali<br>`irekiFormularioa()`: Bisita berria/editatu formularioa ireki |
| **BisitaFormularioActivity** | Bisita berria/editatu formularioa | `baliozkotuFormularioa()`: Datuen balidazioa (hutsen kontrolak, formatuak)<br>`gordeBisita()`: Bisita gorde transakzio seguru batean<br>`erakutsiDataHautatzailea()`: MaterialDatePicker erabiliz data hautatu<br>`erakutsiOrduaHautatzailea()`: MaterialTimePicker erabiliz ordua hautatu |
| **EskaerakActivity** | Eskaeren zerrenda pantaila | `kargatuEskaerak()`: **SEGURTASUNA** - bakarrik uneko komertzialaren eskaerak<br>`adapter.eguneratuZerrenda()`: Eskaeren zerrenda RecyclerView-n erakutsi |
| **BazkideaFormularioActivity** | Bazkide berria/editatu formularioa | `erakutsiGordeBaieztapena()`: Datuak gorde aurretik baieztapena<br>`erakutsiEzabatuBaieztapena()`: Bazkidea ezabatu aurretik baieztapena<br>`gordeBazkidea()`: Bazkidea gorde datu-basean (upsert logika) |
| **ZitaGehituActivity** | Zita berria (EskaeraGoiburua) gehitzeko | `gordeCita()`: Zita gorde datu-basean<br>`erakutsiDataHautatzailea()`: Data hautatzailea<br>`erakutsiOrduaHautatzailea()`: Ordua hautatzailea |
| **ProduktuDetalaActivity** | Produktu baten informazioa erakusten du | `beteEdukia()`: Produktuaren datuak erakutsi (irudia, izena, prezioa, stock)<br>`erosiSaskira()`: Produktua saskira gehitu (RESULT_OK bidali) |
| **HistorialCompraActivity** | Erosketen historiala pantaila | `kargatuHistoriala()`: Historial guztiak kargatu datu-basean<br>`adapter.eguneratuZerrenda()`: Historial zerrenda RecyclerView-n erakutsi |

### Room Entities (Datu-base Entitateak)

| Fitxategia | Helburua | Eremu Garrantzitsuenak |
|------------|-------|------------------------|
| **Komertziala** | Komertzialen datu-egitura | `id` (PK, auto), `kodea` (unique), `izena`, `abizena`, `posta`, `jaiotzeData`, `argazkia` |
| **Bazkidea** | Bazkideen datu-egitura | `id` (PK, auto), `nan` (indizea), `izena`, `abizena`, `telefonoZenbakia`, `posta`, `jaiotzeData`, `argazkia`, `kodea`, `helbidea`, `probintzia`, `komertzialKodea` (FK → `komertzialak.kodea`, `ON DELETE CASCADE`), `sortutakoData` |
| **Agenda** | Bisiten agenda entitatea | `id` (PK, auto), `bisitaData`, `ordua`, `komertzialKodea`, `bazkideaKodea`, `bazkideaId` (FK), `komertzialaId` (FK), `deskribapena`, `egoera` |
| **EskaeraGoiburua** | Eskaeraren goiburua | `zenbakia` (PK), `data`, `komertzialKodea`, `komertzialId`, `ordezkaritza`, `bazkideaKodea`, `bazkideaId` |
| **EskaeraXehetasuna** | Eskaeraren xehetasunak | `id` (PK, auto), `eskaeraZenbakia`, `artikuluKodea`, `kantitatea`, `prezioa` |
| **Eskaera** | Eskaera zaharrak (XML formatua) | `id` (PK, auto), `eskaeraID`, `bazkideaId` (FK → `bazkideak.id`, `ON DELETE CASCADE`), `prodIzena`, `data`, `kopurua`, `prodArgazkia` |
| **Katalogoa** | Produktuen katalogoa | `artikuluKodea` (PK), `izena`, `salmentaPrezioa`, `stock`, `irudiaIzena` |
| **Logina** | Erabiltzaile/pasahitza sarbideak | `id` (PK, auto), `erabiltzailea`, `pasahitza`, `komertzialKodea` |
| **HistorialCompra** | Erosketen historiala (bidalketa XML) | `id` (PK, auto), `bidalketaId`, `kodea`, `helmuga`, `data`, `amaituta`, `productoId`, `productoIzena`, `eskatuta`, `bidalita`, `prezioUnit`, `argazkia` |

### DAOs (Data Access Objects)

| Fitxategia | Helburua | Kontsulta Garrantzitsuenak |
|------------|----------|----------------------------|
| **AgendaDao** | Agenda (bisitak) taularen kontsultak | `getVisitsByKomertzial()`: **SEGURTASUNA** - komertzial baten bisitak bakarrik<br>`bilatuBezeroaz()`: Bazkide kodea/izenaren arabera bilatu<br>`bilatuDataTarteaz()`: Data tartearen arabera bilatu<br>`bilatuOrokorra()`: Bilaketa orokorra (data, izena, deskribapena, egoera)<br>`idzBilatuSegurua()`: **SEGURTASUNA** - ID bidezko bilaketa segurua |
| **BazkideaDao** | Bazkideak taularen kontsultak | `nanBilatu()`: NAN baten arabera bazkidea bilatu<br>`bilatu()`: Bilatzailea (NAN, izena, abizena, posta, telefonoa)<br>`txertatu()`: Upsert logika (`OnConflictStrategy.REPLACE`)<br>`ezabatuIdakEzDirenak()`: Sinkronizazioa (id zerrenda = mantendu behar diren id-ak) |
| **EskaeraGoiburuaDao** | Eskaera goiburuak taularen kontsultak | `komertzialarenEskaerak()`: Komertzial kode baten arabera eskaerak<br>`egunekoEskaerak()`: Eguneko eskaerak (esportazioa)<br>`hilabetekoEskaerak()`: Hilabeteko eskaerak (agenda esportazioa) |
| **EskaeraXehetasunaDao** | Eskaera xehetasunak taularen kontsultak | `eskaerarenXehetasunak()`: Eskaera zenbaki baten arabera xehetasunak<br>`txertatuGuztiak()`: Hainbat xehetasun txertatu transakzioan |
| **EskaeraDao** | Eskaera zaharrak taularen kontsultak | `bazkidearenEskaerak()`: Bazkide ID baten arabera eskaerak<br>`ezabatuBazkidearenEskaerak()`: Bazkide baten eskaera guztiak ezabatu (sinkronizazioa) |
| **KatalogoaDao** | Katalogoa taularen kontsultak | `artikuluaBilatu()`: Artikulu kodea baten arabera produktua bilatu<br>`guztiak()`: Produktu guztiak<br>`stockaEguneratu()`: Stock eguneratu eskaera bat egiten denean |
| **KomertzialaDao** | Komertzialak taularen kontsultak | `kodeaBilatu()`: Kode baten arabera komertziala bilatu<br>`guztiak()`: Komertzial guztiak |
| **LoginaDao** | Loginak taularen kontsultak | `sarbideaBalidatu()`: Erabiltzaile/pasahitza balidazioa |
| **HistorialCompraDao** | HistorialCompra taularen kontsultak | `guztiak()`: Historial guztiak<br>`txertatuGuztiak()`: Hainbat historial txertatu transakzioan |

### Repositories eta Kudeatzaileak

| Fitxategia | Helburua | Funtzio Garrantzitsuenak |
|------------|----------|--------------------------|
| **AgendaRepository** | Room eta UI-aren arteko geruza abstraktua | `txertatuBisita()`: Bisita bat txertatu (upsert)<br>`kargatuBisitak()`: **SEGURTASUNA** - uneko komertzialaren bisitak bakarrik<br>`bilatuBezeroaz()`: **SEGURTASUNA** - bezeroaren arabera bilatu<br>`bilatuDataTarteaz()`: **SEGURTASUNA** - data tartearen arabera bilatu<br>`bilatuOrokorra()`: **SEGURTASUNA** - bilaketa orokorra<br>`ezabatuBisita()`: Bisita ezabatu (segurtasun iragazkiarekin)<br>`ExecutorService` erabiliz hari nagusitik kanpo exekutatzen da |
| **XMLKudeatzailea** | XML fitxategiak inportatzeko kudeatzailea | `komertzialakInportatu()`: Wipe-and-load estrategia<br>`bazkideakInportatu()`: **Upsert logika** - existitzen bada eguneratu, bestela sortu<br>`katalogoaInportatu()`: Wipe-and-load (asteko eguneraketa)<br>`agendaInportatu()`: **Upsert logika** - bisitak transakzio bakar batean<br>`loginakInportatu()`: Loginak inportatu<br>`guztiakInportatu()`: XML guztiak ordena egokian inportatu<br>`XmlPullParser` erabiliz XML parseatu |
| **DatuKudeatzailea** | Esportazio eta inportazio logika koordinatzen du | `bazkideBerriakEsportatu()`: Eguneroko txostena (bazkide berriak)<br>`eskaeraBerriakEsportatu()`: Eguneroko txostena (eskaera berriak)<br>`katalogoaAsterokoInportazioaEgin()`: Asteko inportazioa (katalogoa)<br>`agendaHilerokoEsportazioaEgin()`: Hileroko laburpena (agenda) |
| **XMLEsportatzailea** | Room datu-baseko datuak XML fitxategietara esportatu | `bazkideBerriakEsportatu()`: `bazkide_berriak.xml` sortu<br>`eskaeraBerriakEsportatu()`: `eskaera_berriak.xml` sortu<br>`komertzialakEsportatu()`: `komertzialak.xml` sortu<br>`XmlSerializer` erabiliz XML sortu |
| **AgendaEsportatzailea** | Agenda bi formatutan esportatu | `agendaXMLSortu()`: **SEGURTASUNA** - uneko komertzialaren bisitak bakarrik<br>`agendaTXTSortu()`: Testu-fitxategia (iraurgarria)<br>`barneMemorianLekuNahikoa()`: Fitxategien egiaztapena |
| **InbentarioKudeatzailea** | Katalogoa astero inportatzeko kudeatzailea | `katalogoaAsterokoInportazioaEgin()`: Barne-memoriatik edo assets-etik katalogoa inportatu<br>`katalogoaInportatuFluxutik()`: Sarrera-fluxu batetik katalogoa inportatu |

### Segurtasuna eta Util

| Fitxategia | Helburua | Funtzio Garrantzitsuenak |
|------------|----------|--------------------------|
| **SessionManager** | Saioa hasi duen komertzialaren kodea modu seguruan gordetzen du | `saioaHasi()`: Komertzial kodea eta izena gorde (SharedPreferences)<br>`saioaItxi()`: Saioa itxi (datuak ezabatu)<br>`getKomertzialKodea()`: **SEGURTASUNA** - uneko komertzialaren kodea itzuli<br>`getKomertzialIzena()`: Komertzialaren izena itzuli<br>`saioaHasitaDago()`: Saioa hasita dagoen egiaztatu<br>`kodeaBalidatu()`: **SEGURTASUNA** - kodea uneko saioaren kodea dela egiaztatu |
| **EskaeraBalidatzailea** | Eskaerak datu-basean gordetzeko aurretik datuen integritatea egiaztatzen du | `balidatuEskaera()`: Derrigorrezko eremuak beteta daudela egiaztatu (komertzialKodea, bazkideaKodea)<br>`balidatuEtaGorde()`: Balidatu eta gorde transakzio seguru batean<br>`IllegalArgumentException` jaurtitzen du daturen bat falta bada |
| **XmlBilatzailea** | Assets-en dauden XML fitxategiak bilatzen ditu | `faltaDa()`: Assets-en fitxategi hori badagoen ala ez<br>`faltatzenDiren()`: Falta diren XML fitxategien zerrenda<br>`loginakFaltaDa()`, `komertzialakFaltaDa()`, `bazkideakFaltaDa()`, `katalogoaFaltaDa()`: Fitxategi espezifikoak falta diren egiaztatu |

### Adapters (RecyclerView)

| Fitxategia | Helburua | Funtzio Garrantzitsuenak |
|------------|----------|--------------------------|
| **AgendaBisitaAdapter** | Agenda bisiten zerrenda erakusteko | `eguneratuZerrenda()`: Zerrenda eguneratu<br>`OnBisitaEkintzaListener`: Ikusi, Editatu, Ezabatu botoiak<br>`AgendaElementua`: Bisita datuak eta bazkidearen izena |
| **EskaerakAdapter** | Eskaeren zerrenda erakusteko | `eguneratuZerrenda()`: Eskaeren zerrenda eguneratu<br>`EskaeraElementua`: Zenbakia, data, artikulu kopurua, guztira |
| **KatalogoaAdapter** | Katalogoa (inbentarioa) zerrenda erakusteko | `eguneratuZerrenda()`: Produktuen zerrenda eguneratu<br>`OnErosiClickListener`: Erosi botoia (saskira gehitu)<br>`OnItemClickListener`: Item sakatzean detale orria ireki |
| **SaskiaAdapter** | Erosketa saskiko zerrenda erakusteko | `onBindViewHolder()`: Kopurua aldatu (+/-), elementu bat kendu<br>`onSaskiaAldaketa`: Badge eta guztira eguneratzeko callback |
| **HistorialCompraAdapter** | Erosketa historial zerrenda erakusteko | `eguneratuZerrenda()`: Historial zerrenda eguneratu<br>`HistorialElementua`: Produktua, kantitatea, prezio unitarioa, prezio totala |

### AppDatabase

| Fitxategia | Helburua | Funtzio Garrantzitsuenak |
|------------|----------|--------------------------|
| **AppDatabase** | Room datu-basearen instantzia bakarra (singleton) | `getInstance()`: Singleton patroia (synchronized)<br>`addMigrations()`: 15 bertsio migrazio estrategia<br>`fallbackToDestructiveMigration()`: Eskema aldaketa handia<br>`allowMainThreadQueries()`: Kontsulta bat hari nagusian egiten bada<br>`komertzialaDao()`, `bazkideaDao()`, `agendaDao()`, etab.: DAO interfazeak |

---

*Dokumentazio hau proiektuaren arkitektura, funtzionalitate kritikoak eta fitxategi bakoitzaren erantzukizuna deskribatzen ditu. Kodearen xehetasun gehiago lortzeko, kodea kontsultatu edo garatzaileekin jarri harremanetan.*

