package pl.kondi.fraud;

import org.springframework.data.jpa.repository.JpaRepository;

interface FraudCheckHistoryRepository extends JpaRepository<FraudCheckHistory, Integer> {
}
