package com.movies;

import com.movies.dao.*;
import com.movies.entity.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class Main {

    private final SessionFactory sesssionFactory;

    private final ActorDAO actorDAO;
    private final AddressDAO addressDAO;
    private final CategoryDAO categoryDAO;
    private final CityDAO cityDAO;
    private final CountryDAO countryDAO;
    private final CustomerDAO customerDAO;
    private final FilmDAO filmDAO;
    private final FilmTextDAO filmTextDAO;
    private final InventoryDAO inventoryDAO;
    private final LanguageDAO languageDAO;
    private final PaymentDAO paymentDAO;
    private final RentalDAO rentalDAO;
    private final StaffDAO staffDAO;
    private final StoreDAO storeDAO;

    public Main() {
        Properties properties = new Properties();
        properties.put(Environment.DIALECT,"org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.DRIVER,"com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL,"jdbc:p6spy:mysql://localhost:3306/movie");
        properties.put(Environment.USER,"root");
        properties.put(Environment.PASS,"123456");
        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS,"thread");
        properties.put(Environment.HBM2DDL_AUTO,"validate");

        sesssionFactory = new Configuration()
                .addAnnotatedClass(Actor.class)
                .addAnnotatedClass(Address.class)
                .addAnnotatedClass(Category.class)
                .addAnnotatedClass(City.class)
                .addAnnotatedClass(Country.class)
                .addAnnotatedClass(Customer.class)
                .addAnnotatedClass(Feature.class)
                .addAnnotatedClass(Film.class)
                .addAnnotatedClass(FilmText.class)
                .addAnnotatedClass(Inventory.class)
                .addAnnotatedClass(Language.class)
                .addAnnotatedClass(Payment.class)
                .addAnnotatedClass(Rental.class)
                .addAnnotatedClass(Staff.class)
                .addAnnotatedClass(Store.class)
                .addProperties(properties)
                .buildSessionFactory();

        actorDAO = new ActorDAO(sesssionFactory);
        addressDAO = new AddressDAO(sesssionFactory);
        categoryDAO = new CategoryDAO(sesssionFactory);
        cityDAO = new CityDAO(sesssionFactory);
        countryDAO = new CountryDAO(sesssionFactory);
        customerDAO = new CustomerDAO(sesssionFactory);
        filmDAO = new FilmDAO(sesssionFactory);
        filmTextDAO = new FilmTextDAO(sesssionFactory);
        inventoryDAO = new InventoryDAO(sesssionFactory);
        languageDAO = new LanguageDAO(sesssionFactory);
        paymentDAO = new PaymentDAO(sesssionFactory);
        rentalDAO = new RentalDAO(sesssionFactory);
        staffDAO = new StaffDAO(sesssionFactory);
        storeDAO = new StoreDAO(sesssionFactory);

    }

    public static void main(String[] args) {


        Main main = new Main();
        Customer customer = main.createCustomer();

        main.customerReturnInventoryToStore();
        
        main.customerRentInventory(customer);

        main.newFilm();
    }

    private void newFilm() {

        try (Session session = sesssionFactory.getCurrentSession()) {
            session.beginTransaction();

            Language language = languageDAO.getItems(0,20).stream().unordered().findAny().get();
            List<Category> categories = categoryDAO.getItems(0,5);
            List<Actor> actors = actorDAO.getItems(0,10);

            Film film = new Film();

            film.setCategories(new HashSet<>(categories));
            film.setLanguage(language);
            film.setActors(new HashSet<>(actors));
            film.setSpecialFeatures(Set.of(Feature.TRAILERS,Feature.BEHINDTHESCENES));
            film.setLength((short) 123);
            film.setReplacementCost(BigDecimal.TEN);
            film.setRentalRate(BigDecimal.ZERO);
            film.setDescription("some film");
            film.setRating(Rating.NC17);
            film.setTitle("film");
            film.setRentalDuration((byte) 11);
            film.setOriginalLanguage(language);
            film.setYear(Year.now());

            filmDAO.save(film);

            FilmText filmText = new FilmText();
            filmText.setId(film.getId());
            filmText.setFilm(film);
            filmText.setDescription("some film");
            filmText.setTitle("film");

            filmTextDAO.save(filmText);


            session.getTransaction().commit();
        }
    }

    private void customerRentInventory(Customer customer) {

        try (Session session = sesssionFactory.getCurrentSession()) {
            session.beginTransaction();

            Film film = filmDAO.getAvailable();

            Store store = storeDAO.getItems(0,1).get(0);

            Inventory inventory = new Inventory();
            inventory.setFilm(film);
            inventory.setStore(store);

            inventoryDAO.save(inventory);

            Staff staff = store.getStaff();

            Rental rental = new Rental();
            rental.setCustomer(customer);
            rental.setInventory(inventory);
            rental.setRentlaDate(LocalDateTime.now());
            rental.setStaff(staff);

            rentalDAO.save(rental);

            Payment payment = new Payment();
            payment.setCustomer(customer);
            payment.setStaff(staff);
            payment.setRental(rental);
            payment.setAmount(BigDecimal.valueOf(55));

            paymentDAO.save(payment);

            session.getTransaction().commit();
        }
    }

    private void customerReturnInventoryToStore() {
        try (Session session = sesssionFactory.getCurrentSession()) {
            session.beginTransaction();

            Rental rental = rentalDAO.getAnyUnreturnedRental();
            rental.setReturnDate(LocalDateTime.now());

            rentalDAO.save(rental);

            session.getTransaction().commit();
        }
    }

    private Customer createCustomer() {

        try (Session session = sesssionFactory.getCurrentSession()){
            session.beginTransaction();

            Store store = storeDAO.getItems(0,1).get(0);

            City city = cityDAO.getByName("Ashgabat");

            Address address = new Address();

            address.setAddress("47 MySakila Drive");
            address.setDistrict("Alberta");
            address.setCity(city);
            address.setPhone("14033335568");

            addressDAO.save(address);

            Customer customer = new Customer();
            customer.setAddress(address);
            customer.setStore(store);
            customer.setActive(true);
            customer.setFirstName("Test");
            customer.setLastName("Test_L");

            customerDAO.save(customer);

            session.getTransaction().commit();

            return customer;
        }
    }
}