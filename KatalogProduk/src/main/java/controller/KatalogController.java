package controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import data.ProdukRepository;
import model.Produk;

public class KatalogController {

    @FXML
    private TextField namaField;
    @FXML
    private TextField hargaField;
    @FXML
    private ComboBox<String> fotoBox;
    @FXML
    private Label pesanLabel;
    @FXML
    private Label jumlahLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    private Long editingId;
    @FXML
    private TilePane katalogPane;
    private final ProdukRepository repository = new ProdukRepository();

    @FXML
    private void initialize() {
        fotoBox.getItems().add("roti.png");
        fotoBox.getItems().add("susu.png");
        fotoBox.getItems().add("kopi.png");
        fotoBox.setValue("roti.png");
        try {
            showData();
        } catch (SQLException e) {
            pesanLabel.setText("Gagal membaca database. Lihat Output.");
            e.printStackTrace();
        }
    }

    private void showData() throws SQLException {
        ArrayList<Produk> daftar = repository.getAll();
        katalogPane.getChildren().clear();
        for (Produk produk : daftar) {
            VBox kartu = createProductCard(produk);
            katalogPane.getChildren().add(kartu);
        }
        jumlahLabel.setText("Jumlah produk: " + daftar.size());
    }

    private VBox createProductCard(Produk produk) {
        ImageView fotoView = new ImageView();
        fotoView.setFitWidth(170);
        fotoView.setFitHeight(110);
        fotoView.setPreserveRatio(true);
        StackPane bingkai = new StackPane();
        bingkai.setPrefSize(180, 120);
        bingkai.setMinSize(180, 120);
        bingkai.setMaxSize(180, 120);

        URL lokasi = getClass().getResource("/image/" + produk.getFoto());
        if (lokasi != null) {
            Image gambar = new Image(lokasi.toExternalForm(),
                    170, 110, true, true);
            if (!gambar.isError()) {
                fotoView.setImage(gambar);
                bingkai.getChildren().add(fotoView);
            } else {
                bingkai.getChildren().add(new Label("Gambar tidak terbaca"));
            }
        } else {
            bingkai.getChildren().add(new Label("Gambar tidak tersedia"));
        }

        Label namaLabel = new Label(produk.getNama());
        namaLabel.setWrapText(true);
        namaLabel.getStyleClass().add("nama-produk");
        Label hargaLabel = new Label("Rp" + produk.getHarga());
        Label idLabel = new Label("ID: " + produk.getId());
        idLabel.getStyleClass().add("muted");

        VBox kartu = new VBox();
        kartu.setSpacing(8);
        kartu.setMinWidth(220);
        kartu.setPrefWidth(220);
        kartu.setMaxWidth(220);
        kartu.getStyleClass().add("kartu-produk");
        kartu.getChildren().add(bingkai);
        kartu.getChildren().add(namaLabel);
        kartu.getChildren().add(hargaLabel);
        kartu.getChildren().add(idLabel);

        Button editButton = new Button("Edit");
        editButton.setOnAction(event -> editProduct(produk));
        Button deleteButton = new Button("Hapus");
        deleteButton.setOnAction(event -> deleteProduct(produk));
        HBox actions = new HBox(8, editButton, deleteButton);
        kartu.getChildren().add(actions);
        return kartu;
    }

    @FXML
    private void saveProduct() {
        Produk produk;
        String nama;
        long harga;
        String foto;
        try {
            nama = namaField.getText();
            harga = Long.parseLong(hargaField.getText().trim());
            foto = fotoBox.getValue();
            produk = new Produk(nama, harga, foto);
        } catch (NumberFormatException e) {
            pesanLabel.setText("Harga harus angka bulat, contoh 12000.");
            return;
        } catch (IllegalArgumentException e) {
            pesanLabel.setText(e.getMessage());
            return;
        }
        try {
            if (editingId == null) {
                repository.add(produk);
                pesanLabel.setText("Produk berhasil disimpan.");
            } else {
                Produk produkEdit = new Produk(editingId, nama, harga, foto);
                repository.update(produkEdit);
                pesanLabel.setText("Produk berhasil diperbarui.");
            }
        } catch (SQLException e) {
            pesanLabel.setText("Gagal menyimpan. Lihat Output.");
            e.printStackTrace();
            return;
        } catch (IllegalArgumentException e) {
            pesanLabel.setText(e.getMessage());
            return;
        }
        resetForm();
        try {
            showData();
        } catch (SQLException e) {
            pesanLabel.setText("Data tersimpan, tetapi katalog gagal dimuat ulang.");
            e.printStackTrace();
        }
    }

    private void editProduct(Produk produk) {
        editingId = produk.getId();
        namaField.setText(produk.getNama());
        hargaField.setText(String.valueOf(produk.getHarga()));
        fotoBox.setValue(produk.getFoto());
        saveButton.setText("Perbarui");
        cancelButton.setVisible(true);
        cancelButton.setManaged(true);
        pesanLabel.setText("Ubah data produk, lalu klik Perbarui.");
    }

    private void deleteProduct(Produk produk) {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Hapus Produk");
        konfirmasi.setHeaderText("Hapus produk " + produk.getNama() + "?");
        konfirmasi.setContentText("Data produk yang dihapus tidak dapat dikembalikan.");
        if (konfirmasi.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                repository.delete(produk.getId());
                if (editingId != null && editingId == produk.getId()) {
                    resetForm();
                }
                showData();
                pesanLabel.setText("Produk berhasil dihapus.");
            } catch (SQLException e) {
                pesanLabel.setText("Gagal menghapus produk. Lihat Output.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void cancelEdit() {
        resetForm();
        pesanLabel.setText("Edit produk dibatalkan.");
    }

    private void resetForm() {
        editingId = null;
        namaField.clear();
        hargaField.clear();
        fotoBox.setValue("roti.png");
        saveButton.setText("Simpan");
        cancelButton.setVisible(false);
        cancelButton.setManaged(false);
    }

}
