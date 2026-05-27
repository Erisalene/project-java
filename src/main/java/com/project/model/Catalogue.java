package com.project.model;

import com.project.exception.BookNotFoundException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Catalogue {
    private List<Publication> publications;

    public Catalogue() {
        this.publications = new ArrayList<>();
    }

    public void addPublication(Publication p) {
        if (p != null) {
            publications.add(p);
        }
    }

    public void removePublicationByTitle(String title) throws BookNotFoundException {
        Publication toRemove = findPublicationByTitle(title);
        if (toRemove == null) {
            throw new BookNotFoundException("Неможливо видалити: публікацію з назвою \"" + title + "\" не знайдено.");
        }
        publications.remove(toRemove);
    }

    public Publication findPublicationByTitle(String title) {
        for (Publication p : publications) {
            if (p.getTitle().equalsIgnoreCase(title.trim())) {
                return p;
            }
        }
        return null;
    }

    public List<Publication> getAllPublications() {
        return new ArrayList<>(publications);
    }

    public void saveToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(publications);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                this.publications = (List<Publication>) obj;
            }
        }
    }
}
