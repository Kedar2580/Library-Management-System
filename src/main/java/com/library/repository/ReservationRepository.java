package com.library.repository;

import com.library.model.Book;
import com.library.model.Reservation;
import com.library.model.ReservationStatus;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByStatusOrderByReservedAtAsc(ReservationStatus status);

    List<Reservation> findByMemberOrderByReservedAtDesc(User member);

    Optional<Reservation> findByBookAndMemberAndStatus(Book book, User member, ReservationStatus status);

    List<Reservation> findByBookAndStatusOrderByReservedAtAsc(Book book, ReservationStatus status);

    long countByStatus(ReservationStatus status);
}
