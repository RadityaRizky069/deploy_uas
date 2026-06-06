# 4 Pilar OOP pada Project Trashformer

## 1. Encapsulation (Enkapsulasi)

**Pengertian:**
Encapsulation adalah teknik menyembunyikan data (atribut) di dalam class dengan cara membuat atribut bersifat private, lalu mengaksesnya melalui method getter dan setter. Tujuannya adalah melindungi data agar tidak bisa diubah sembarangan dari luar class.

**Contoh di Project:**

- **Lokasi file:** `src\main\java\com\example\trashformer\model\User.java`

- **Potongan kode:**
```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nama;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(precision = 15, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    // Getter dan Setter
    public Long getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BigDecimal getSaldo() { 
        return saldo; 
    }
    
    public void setSaldo(BigDecimal saldo) { 
        this.saldo = saldo; 
    }
}
```

- **Penjelasan:**
Class `User` menerapkan encapsulation dengan membuat semua atribut seperti `id`, `nama`, `username`, `password`, `role`, dan `saldo` bersifat **private**. Data ini tidak bisa diakses langsung dari luar class, tetapi harus melalui method **getter** (untuk membaca data) dan **setter** (untuk mengubah data). Dengan cara ini, kita bisa mengontrol bagaimana data diakses dan diubah, sehingga data lebih aman dan terlindungi.

---

## 2. Inheritance (Pewarisan)

**Pengertian:**
Inheritance adalah konsep di mana sebuah class atau interface (child/subclass) mewarisi sifat dan perilaku dari class atau interface lain (parent/superclass). Class anak otomatis mendapatkan semua method dari class induk, dan bisa menambahkan method baru sesuai kebutuhannya.

**Contoh di Project:**

- **Lokasi file:** `src\main\java\com\example\trashformer\repository\UserRepository.java`

- **Parent class:** `JpaRepository<User, Long>` (interface dari Spring Data JPA)

- **Child class:** `UserRepository`

- **Potongan kode:**
```java
package com.example.trashformer.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.trashformer.model.Role;
import com.example.trashformer.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    List<User> findByRole(Role role);

    List<User> findByNamaContainingIgnoreCaseOrUsernameContainingIgnoreCase(String nama, String username);

    long countByRole(Role role);
}
```

- **Penjelasan:**
Interface `UserRepository` **extends** (mewarisi) `JpaRepository<User, Long>`. Ini adalah contoh inheritance yang jelas. `JpaRepository` adalah parent yang sudah punya banyak method bawaan seperti `save()`, `findAll()`, `findById()`, `delete()`, dll. Karena `UserRepository` extends dari `JpaRepository`, maka `UserRepository` otomatis mendapatkan semua method tersebut tanpa perlu menulis ulang. Lalu `UserRepository` juga menambahkan method baru sesuai kebutuhan project seperti `findByUsername()` dan `findByRole()`. Ini adalah contoh nyata inheritance: anak mewarisi kemampuan orang tua, lalu menambah kemampuan sendiri.

---

## 3. Polymorphism (Polimorfisme)

**Pengertian:**
Polymorphism artinya "banyak bentuk". Dalam OOP, polymorphism memungkinkan method yang sama bisa berperilaku berbeda. Contoh sederhananya: method `save()` bisa digunakan untuk menyimpan User, Setoran, atau data apapun, tapi cara kerjanya disesuaikan dengan tipe datanya.

**Contoh di Project:**

- **Lokasi file:** `src\main\java\com\example\trashformer\service\BankSampahService.java`

- **Jenis polymorphism:** Penggunaan method yang sama dengan perilaku berbeda

- **Potongan kode:**
```java
@Service
public class BankSampahService {

    private final UserRepository userRepository;
    private final SaldoTransaksiRepository saldoTransaksiRepository;

    // Method untuk MENAMBAH saldo (KREDIT)
    @Transactional
    public SaldoTransaksi kreditSaldo(Long userId, BigDecimal jumlah, String keterangan, Long setoranId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        BigDecimal saldoSebelum = user.getSaldo() != null ? user.getSaldo() : BigDecimal.ZERO;
        BigDecimal saldoSesudah = saldoSebelum.add(jumlah);  // TAMBAH saldo
        user.setSaldo(saldoSesudah);
        userRepository.save(user);

        SaldoTransaksi trx = new SaldoTransaksi();
        trx.setUser(user);
        trx.setTipe(TipeTransaksi.KREDIT);
        trx.setJumlah(jumlah);
        trx.setSaldoSebelum(saldoSebelum);
        trx.setSaldoSesudah(saldoSesudah);
        trx.setKeterangan(keterangan);
        trx.setSetoranId(setoranId);
        return saldoTransaksiRepository.save(trx);
    }

    // Method untuk MENGURANGI saldo (DEBIT)
    @Transactional
    public SaldoTransaksi debitSaldo(Long userId, BigDecimal jumlah, String keterangan) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        BigDecimal saldoSebelum = user.getSaldo() != null ? user.getSaldo() : BigDecimal.ZERO;
        if (saldoSebelum.compareTo(jumlah) < 0) {
            throw new RuntimeException("Saldo tidak mencukupi");  // Validasi khusus
        }
        BigDecimal saldoSesudah = saldoSebelum.subtract(jumlah);  // KURANGI saldo
        user.setSaldo(saldoSesudah);
        userRepository.save(user);

        SaldoTransaksi trx = new SaldoTransaksi();
        trx.setUser(user);
        trx.setTipe(TipeTransaksi.DEBIT);
        trx.setJumlah(jumlah);
        trx.setSaldoSebelum(saldoSebelum);
        trx.setSaldoSesudah(saldoSesudah);
        trx.setKeterangan(keterangan);
        return saldoTransaksiRepository.save(trx);
    }
}
```

- **Penjelasan:**
Ini contoh polymorphism sederhana. Ada dua method untuk mengelola saldo: `kreditSaldo()` dan `debitSaldo()`. Keduanya sama-sama mengelola saldo user, tapi **perilakunya berbeda**:
- `kreditSaldo()` → menambah saldo (pakai `.add()`)
- `debitSaldo()` → mengurangi saldo (pakai `.subtract()`) dan punya validasi khusus untuk cek saldo cukup atau tidak

Keduanya sama-sama menyimpan transaksi ke database, tapi logika bisnisnya berbeda. Ini adalah polymorphism: satu konsep (kelola saldo) dengan banyak bentuk perilaku.

---

## 4. Abstraction (Abstraksi)

**Pengertian:**
Abstraction adalah proses menyembunyikan detail rumit dan hanya menampilkan fungsi-fungsi yang penting. Kita tidak perlu tahu "bagaimana cara kerjanya", cukup tahu "apa yang bisa dilakukan". Seperti saat kita pakai remote TV, kita tidak perlu tahu gimana remote bekerja secara teknis, yang penting kita tahu tombol mana untuk ganti channel.

**Contoh di Project:**

- **Lokasi file:** `src\main\java\com\example\trashformer\repository\UserRepository.java`

- **Bentuk abstraction:** Interface Repository (menyembunyikan detail query database)

- **Potongan kode:**
```java
package com.example.trashformer.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.trashformer.model.Role;
import com.example.trashformer.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    List<User> findByRole(Role role);

    long countByRole(Role role);
}
```

- **Penjelasan:**
Interface `UserRepository` adalah contoh abstraction. Lihat method `findByUsername(String username)` - kita cukup panggil method ini dengan parameter username, lalu otomatis dapat data user. Kita **tidak perlu tahu**:
- Query SQL-nya seperti apa
- Bagaimana koneksi ke database
- Bagaimana handle error database
- Bagaimana mapping data dari tabel ke object Java

Semua detail rumit tersebut **disembunyikan** oleh Spring Data JPA. Programmer hanya perlu tahu: "ada method `findByUsername()`, tinggal pakai aja". Ini adalah abstraction: sembunyikan yang rumit, tampilkan yang simple.

**Contoh Abstraction Lain (Service Layer):**

- **Lokasi file:** `src\main\java\com\example\trashformer\service\UserService.java`

- **Bentuk abstraction:** Service Layer (menyembunyikan logic bisnis)

- **Potongan kode:**
```java
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerWarga(String nama, String username, String password) {
        User user = new User();
        user.setNama(nama.trim());
        user.setUsername(username.trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(password.trim()));
        user.setRole(Role.WARGA);
        user.setActive(true);
        return userRepository.save(user);
    }

    public void toggleActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        boolean currentStatus = user.isActive() == null || user.isActive();
        user.setActive(!currentStatus);
        userRepository.save(user);
    }
}
```

- **Penjelasan:**
Method `registerWarga()` menyembunyikan proses rumit pendaftaran user. Controller yang memanggil method ini **tidak perlu tahu** detail seperti:
- Harus trim (bersihkan) input dulu
- Username harus lowercase
- Password harus di-encode pakai BCrypt
- Role harus di-set jadi WARGA
- Status aktif harus true
- Baru simpan ke database

Controller cukup panggil `registerWarga(nama, username, password)` dan semua proses rumit di atas otomatis dikerjakan. Method `toggleActive()` juga sama, menyembunyikan logic untuk mengaktifkan/menonaktifkan user. Ini adalah abstraction: bikin kode yang gampang dipakai, sembunyikan yang ribet.

---

# Kesimpulan

Project **Trashformer** ini sudah menerapkan **4 pilar OOP** dengan baik:

1. **Encapsulation** - Diterapkan di semua model class seperti `User.java`, dengan semua atribut dibuat private dan diakses pakai getter/setter.

2. **Inheritance** - Diterapkan pada `UserRepository.java` yang **extends** `JpaRepository`, sehingga otomatis dapat semua method dari parent seperti `save()`, `findAll()`, `findById()`, dll.

3. **Polymorphism** - Diterapkan pada `BankSampahService.java` dengan method `kreditSaldo()` dan `debitSaldo()` yang sama-sama mengelola saldo tapi dengan perilaku berbeda (tambah vs kurang).

4. **Abstraction** - Diterapkan pada `UserRepository.java` dan `UserService.java` yang menyembunyikan detail rumit (query database, encode password, dll) dan hanya menampilkan method yang mudah dipakai.

Dengan menerapkan 4 pilar OOP ini, project menjadi lebih rapi, mudah dibaca, dan mudah dikembangkan.
