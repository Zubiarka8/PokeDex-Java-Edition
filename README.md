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

*Dokumentazio hau proiektuaren arkitektura eta funtzionalitate kritikoak deskribatzen ditu. Kodearen xehetasun gehiago lortzeko, kodea kontsultatu edo garatzaileekin jarri harremanetan.*

