import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Interface yang mendefinisikan aksi-aksi yang dapat dilakukan terhadap inventaris
interface InventoryActions {
    void addItem(); // Menambahkan barang
    void updateItem(); // Memperbarui barang
    void deleteItem(); // Menghapus barang
    void viewItems(); // Melihat daftar barang
}

// Superclass untuk mendeskripsikan item barang
class InventoryItem {
    protected int idBarang; // ID barang
    protected String namaBarang; // Nama barang
    protected int jumlahBarang; // Jumlah barang
    protected LocalDateTime tanggalMasuk; // Tanggal barang masuk ke sistem

    // Konstruktor untuk menginisialisasi atribut barang
    public InventoryItem(int idBarang, String namaBarang, int jumlahBarang, LocalDateTime tanggalMasuk) {
        this.idBarang = idBarang;
        this.namaBarang = namaBarang;
        this.jumlahBarang = jumlahBarang;
        this.tanggalMasuk = tanggalMasuk;
    }

    // Mengembalikan informasi lengkap tentang barang dalam format string
    public String getDetails() {
        return String.format("ID Barang: %d, Nama Barang: %s, Jumlah Barang: %d, Tanggal Masuk: %s", 
                idBarang, namaBarang, jumlahBarang, tanggalMasuk.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}

// Implementasi dari interface InventoryActions
class InventoryManager implements InventoryActions {
    private Connection connection; // Koneksi ke database
    private ArrayList<InventoryItem> inventoryList; // Daftar barang dalam ArrayList

    // Konstruktor untuk inisialisasi koneksi ke database dan ArrayList
    public InventoryManager() {
        inventoryList = new ArrayList<>();
        try {
            // Menghubungkan ke database PostgreSQL
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/restaurant", "postgres", "postgresql1");
        } catch (SQLException e) {
            System.out.println("Koneksi ke database gagal: " + e.getMessage());
        }
    }

    // Metode untuk menambahkan barang ke dalam inventaris
    @Override
    public void addItem() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                // Meminta input dari pengguna untuk ID, nama, dan jumlah barang
                System.out.print("Masukkan ID Barang: ");
                int idBarang = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                System.out.print("Masukkan Nama Barang: ");
                String namaBarang = scanner.nextLine();
                System.out.print("Masukkan Jumlah Barang: ");
                int jumlahBarang = scanner.nextInt();

                // Manipulasi String: Nama Barang ke huruf kapital
                namaBarang = namaBarang.toUpperCase();

                // Manipulasi Date: Tanggal masuk barang
                LocalDateTime tanggalMasuk = LocalDateTime.now();

                // Menyimpan barang ke database (tanpa tanggalMasuk)
                String sql = "INSERT INTO inventory (id_barang, nama_barang, jumlah_barang) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setInt(1, idBarang);
                    pstmt.setString(2, namaBarang);
                    pstmt.setInt(3, jumlahBarang);
                    pstmt.executeUpdate();
                }

                // Menyimpan barang ke ArrayList
                inventoryList.add(new InventoryItem(idBarang, namaBarang, jumlahBarang, tanggalMasuk));

                System.out.println("Barang berhasil ditambahkan!");
                System.out.println("Tanggal Barang Ditambahkan: " + tanggalMasuk.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                break;
            } catch (InputMismatchException e) {
                // Menangani kesalahan input jika pengguna memasukkan data yang salah
                System.out.println("Input tidak valid. Pastikan Anda memasukkan angka untuk ID dan jumlah barang.");
                scanner.nextLine(); // Membersihkan input yang salah
            } catch (SQLException e) {
                // Menangani kesalahan database
                System.out.println("Terjadi kesalahan saat menambahkan barang: " + e.getMessage());
                break;
            }
        }
    }

    // Metode untuk memperbarui jumlah barang
    @Override
    public void updateItem() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                // Meminta ID barang dan jumlah baru
                System.out.print("Masukkan ID Barang yang akan diperbarui: ");
                int idBarang = scanner.nextInt();
                System.out.print("Masukkan Jumlah Barang Baru: ");
                int jumlahBaru = scanner.nextInt();

                // Update data di database
                String sql = "UPDATE inventory SET jumlah_barang = ? WHERE id_barang = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setInt(1, jumlahBaru);
                    pstmt.setInt(2, idBarang);
                    pstmt.executeUpdate();
                }

                // Update data di ArrayList
                for (InventoryItem item : inventoryList) {
                    if (item.idBarang == idBarang) {
                        item.jumlahBarang = jumlahBaru;
                        break;
                    }
                }

                System.out.println("Barang berhasil diperbarui!");
                break;
            } catch (InputMismatchException e) {
                System.out.println("Input tidak valid. Pastikan Anda memasukkan angka.");
                scanner.nextLine(); // Membersihkan input yang salah
            } catch (SQLException e) {
                System.out.println("Terjadi kesalahan saat memperbarui barang: " + e.getMessage());
                break;
            }
        }
    }

    // Metode untuk menghapus barang dari inventaris
    @Override
    public void deleteItem() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                // Meminta ID barang yang akan dihapus
                System.out.print("Masukkan ID Barang yang akan dihapus: ");
                int idBarang = scanner.nextInt();

                // Menghapus barang dari database
                String sql = "DELETE FROM inventory WHERE id_barang = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setInt(1, idBarang);
                    pstmt.executeUpdate();
                }

                // Menghapus barang dari ArrayList
                inventoryList.removeIf(item -> item.idBarang == idBarang);

                System.out.println("Barang berhasil dihapus!");
                break;
            } catch (InputMismatchException e) {
                System.out.println("Input tidak valid. Pastikan Anda memasukkan angka.");
                scanner.nextLine(); // Membersihkan input yang salah
            } catch (SQLException e) {
                System.out.println("Terjadi kesalahan saat menghapus barang: " + e.getMessage());
                break;
            }
        }
    }

    // Metode untuk melihat semua barang dalam inventaris
    @Override
    public void viewItems() {
        try {
            // Mengosongkan ArrayList sebelum mengambil data baru
            inventoryList.clear();

            // Mengambil data dari database dan menambahkan ke ArrayList
            String sql = "SELECT * FROM inventory";
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    int idBarang = rs.getInt("id_barang");
                    String namaBarang = rs.getString("nama_barang");
                    int jumlahBarang = rs.getInt("jumlah_barang");

                    // Menghitung tanggalMasuk untuk setiap barang (di sini diset ke waktu sekarang)
                    LocalDateTime tanggalMasukLocal = LocalDateTime.now();

                    inventoryList.add(new InventoryItem(idBarang, namaBarang, jumlahBarang, tanggalMasukLocal));
                }
            }

            // Menampilkan semua barang dalam ArrayList
            for (InventoryItem item : inventoryList) {
                System.out.println(item.getDetails());
            }
        } catch (SQLException e) {
            System.out.println("Terjadi kesalahan saat menampilkan barang: " + e.getMessage());
        }
    }
}

// Kelas utama untuk menjalankan aplikasi manajemen inventaris
public class RestaurantInventory {
    public static void main(String[] args) {
        InventoryManager manager = new InventoryManager(); // Membuat objek manajer inventaris
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            // Menampilkan menu dengan tanggal dan waktu saat ini
            LocalDateTime currentTime = LocalDateTime.now();
            System.out.println("\n--- Manajemen Inventaris ---");
            System.out.println("Tanggal dan Waktu: " + currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            System.out.println("1. Tambah Barang");
            System.out.println("2. Perbarui Barang");
            System.out.println("3. Hapus Barang");
            System.out.println("4. Lihat Barang");
            System.out.println("5. Keluar");
            System.out.print("Masukkan pilihan Anda: ");

            try {
                choice = scanner.nextInt();

                // Memilih aksi sesuai dengan input pilihan pengguna
                switch (choice) {
                    case 1:
                        manager.addItem(); // Menambah barang
                        break;
                    case 2:
                        manager.updateItem(); // Memperbarui barang
                        break;
                    case 3:
                        manager.deleteItem(); // Menghapus barang
                        break;
                    case 4:
                        manager.viewItems(); // Melihat daftar barang
                        break;
                    case 5:
                        System.out.println("Keluar..."); // Keluar dari program
                        break;
                    default:
                        System.out.println("Pilihan tidak valid. Silakan coba lagi.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Pilihan harus berupa angka antara 1-5. Silakan coba lagi.");
                scanner.nextLine(); // Membersihkan input yang salah
                choice = -1; // Memastikan loop terus berjalan
            }
        } while (choice != 5); // Program terus berjalan hingga pilihan 5 (Keluar)
    }
}
