package com.nightchallenge.backend.product.config;

import com.nightchallenge.backend.product.domain.Product;
import com.nightchallenge.backend.product.domain.ProductCategory;
import com.nightchallenge.backend.product.domain.ProductOption;
import com.nightchallenge.backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 용도: 제품 초기 데이터 등록.
 * 애플리케이션 시작 시 명세에 확정된 제품과 옵션을 DB에 한 번만 저장한다.
 */
@Component
@RequiredArgsConstructor
public class ProductDataInitializer implements ApplicationRunner {

    private final ProductRepository productRepository;

    /**
     * 용도: 애플리케이션 시작 시 초기 데이터 구성.
     * 카테고리 데이터가 이미 있으면 건너뛰어 서버 재시작에 따른 중복 생성을 방지한다.
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initializeBagProducts();
        initializeTravelProducts();
        initializeFashionAccessoryProducts();
        initializeLifestyleProducts();
    }

    /**
     * 용도: 가방 카테고리 초기 데이터 등록.
     * API 명세에 확정된 하나의 제품과 갈색·분홍·검정 옵션 세 개를 구성한다.
     */
    private void initializeBagProducts() {
        if (productRepository.existsByCategory(ProductCategory.BAG)) {
            return;
        }

        Product product = new Product(
                ProductCategory.BAG,
                String.join("\n\n",
                        "천연 가죽 트림이 더해진 비세토스 모노그램 캔버스 토트백",
                        "학생부터 직장인까지, 누구나 언제 어디서나 실용적으로 활용할 수 있도록 디자인된 라지 토트백입니다.",
                        "시그니처 비세토스 모노그램의 타임리스한 아름다움이 담겨 있으며, 24K 도금 로고 브라스 플레이트 장식이 고급스러움을 더합니다.",
                        "문서, 노트북, 데일리 아이템까지 넉넉하게 수납할 수 있는 실용적인 내부 공간도 갖추고 있습니다."
                )
        );
        product.addOption(new ProductOption("L Aren 비세토스 스쿨 토트", "갈색", 1_250_000));
        product.addOption(new ProductOption("L Aren 비세토스 스쿨 토트", "분홍", 1_250_000));
        product.addOption(new ProductOption("L Aren 비세토스 스쿨 토트", "검정", 1_250_000));

        productRepository.save(product);
    }

    /**
     * 용도: 트래블 카테고리 초기 데이터 등록.
     * 라지 비세토스 수트케이스와 갈색·분홍 옵션 두 개를 구성한다.
     */
    private void initializeTravelProducts() {
        if (productRepository.existsByCategory(ProductCategory.TRAVEL)) {
            return;
        }

        Product product = new Product(
                ProductCategory.TRAVEL,
                String.join("\n\n",
                        "시대를 초월한 캐리어 수공예 기술을 증명합니다.",
                        "70년대 뮌헨의 문화 전성기에 탄생하여 독일 역사에서 유명한 그 시기의 세련된 여행 감성을 구현한 라지 하드케이스 비세토스 수트케이스입니다.",
                        "소가죽으로 감싼 모서리와 티없이 깨끗한 마이크로파이버 스웨이드 소재의 내부 및 24K 도금 래치 잠금 장치가 돋보이는 헤리티지 동반자입니다."
                )
        );
        product.addOption(new ProductOption("L 비세토스 수트케이스", "갈색", 6_750_000));
        product.addOption(new ProductOption("L 비세토스 수트케이스", "분홍", 6_750_000));

        productRepository.save(product);
    }

    /**
     * 용도: 패션소품 카테고리 초기 데이터 등록.
     * 코스믹 스타 오 드 퍼퓸의 50ml와 75ml 옵션별 이름과 가격을 구성한다.
     */
    private void initializeFashionAccessoryProducts() {
        if (productRepository.existsByCategory(ProductCategory.FASHION_ACCESSORY)) {
            return;
        }

        Product product = new Product(
                ProductCategory.FASHION_ACCESSORY,
                String.join("\n\n",
                        "자신만의 빛으로 세상을 밝히는 이들을 위한 향수",
                        "모험가와 꿈꾸는 이들을 위한 천상의 향기 '코즈믹 스타'는 자유분방함과 자신감, 그리고 호기심을 발산합니다.",
                        "우주의 에너지에서 영감을 받은 이 여성용 향수는 페어와 은방울꽃, 코코넛 워터의 산뜻한 탑 노트로 그 빛을 발합니다.",
                        "신선한 꽃향기의 에너지를 품은 '코즈믹 스타'의 진정한 매력은 시간이 흐를수록 그 본연의 향을 풍성하게 드러냅니다.",
                        "그리고 지평선 너머에서 전해오는 바닐라, 화이트 초콜릿, 오크모스의 달콤하고 평온한 잔향 속에 몸을 맡겨보세요."
                )
        );
        product.addOption(new ProductOption("코스믹 스타 오 드 퍼퓸 50ml", "50ml", 118_000));
        product.addOption(new ProductOption("코스믹 스타 오 드 퍼퓸 75ml", "75ml", 141_000));

        productRepository.save(product);
    }

    /**
     * 용도: 라이프스타일 카테고리 초기 데이터 등록.
     * 단일 에어팟 프로 케이스 옵션을 별도의 구분값 없이 구성한다.
     */
    private void initializeLifestyleProducts() {
        if (productRepository.existsByCategory(ProductCategory.LIFESTYLE)) {
            return;
        }

        Product product = new Product(
                ProductCategory.LIFESTYLE,
                String.join("\n\n",
                        "헤리티지 모노그램이 돋보이는 모바일 액세서리 케이스",
                        "풀그레인 나파 가죽으로 제작된 에어팟 프로 케이스. 클래식 비세토스 모노그램이 엠보싱으로 표현되었으며, 바이에른 다이아몬드에서 영감을 받은 스프링 클라스프가 더해져 백에 부착할 수 있습니다."
                )
        );
        product.addOption(new ProductOption(
                "엠보스드 모노그램 레더 에어팟 프로 케이스",
                null,
                310_000
        ));

        productRepository.save(product);
    }
}
