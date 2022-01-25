package br.com.zenitech.emissorweb.domains;

public class PixDomain {
    private String id;
    private String encodedImage;
    private String payload;
    private String expirationDate;
    private String status;

    public PixDomain(String id, String encodedImage, String payload, String expirationDate, String status) {
        this.id = id;
        this.encodedImage = encodedImage;
        this.payload = payload;
        this.expirationDate = expirationDate;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEncodedImage() {
        return encodedImage;
    }

    public void setEncodedImage(String encodedImage) {
        this.encodedImage = encodedImage;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
