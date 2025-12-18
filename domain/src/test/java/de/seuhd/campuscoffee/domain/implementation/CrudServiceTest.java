package de.seuhd.campuscoffee.domain.implementation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import de.seuhd.campuscoffee.domain.configuration.ApprovalConfiguration;
import de.seuhd.campuscoffee.domain.exceptions.NotFoundException;
import de.seuhd.campuscoffee.domain.exceptions.ValidationException;
import de.seuhd.campuscoffee.domain.model.objects.Pos;
import de.seuhd.campuscoffee.domain.model.objects.Review;
import de.seuhd.campuscoffee.domain.model.objects.User;
import de.seuhd.campuscoffee.domain.ports.data.PosDataService;
import de.seuhd.campuscoffee.domain.ports.data.ReviewDataService;
import de.seuhd.campuscoffee.domain.ports.data.UserDataService;
import de.seuhd.campuscoffee.domain.tests.TestFixtures;
import static de.seuhd.campuscoffee.domain.tests.TestFixtures.getApprovalConfiguration;

/**
 * Unit and integration tests for the operations related to reviews.
 */
@ExtendWith(MockitoExtension.class)
public class CrudServiceTest {
    private final ApprovalConfiguration approvalConfiguration = getApprovalConfiguration();

    @Mock
    private ReviewDataService reviewDataService;

    @Mock
    private UserDataService userDataService;

    @Mock
    private PosDataService posDataService;

    private CrudServiceImpl<Review, Long> reviewService;

    @BeforeEach
    void beforeEach() {
        reviewService = new ReviewServiceImpl(
                reviewDataService, userDataService, posDataService, approvalConfiguration
        );
    }

    @Test
    void createReviewPosDoesNotExistException() {
        // given
        Review review = TestFixtures.getReviewFixtures().getFirst();
        assertNotNull(review.pos().getId());
        when(posDataService.getById(review.pos().getId())).thenThrow(
                new NotFoundException(review.pos().getClass(), review.pos().getId())
        );

        // when, then
        assertThrows(NotFoundException.class, () -> reviewService.upsert(review));
        verify(posDataService).getById(review.pos().getId());
    }

    @Test
    void userCannotCreateMoreThanOneReviewPerPos() {
        // given
        Review review = TestFixtures.getReviewFixtures().getFirst();
        Pos pos = review.pos();
        User author = review.author();
        assertNotNull(pos.getId());

        when(posDataService.getById(pos.getId())).thenReturn(pos);
        when(reviewDataService.filter(pos, author)).thenReturn(List.of(review));

        // when, then
        assertThrows(ValidationException.class, () -> reviewService.upsert(review)
        );
        verify(posDataService).getById(pos.getId());
        verify(reviewDataService).filter(pos, author);
    }

    @Test
    void clearReviewServiceCallsDataServiceClear() {
        // when
        reviewService.clear();

        // then
        verify(reviewDataService).clear();
    }

    @Test
    void deleteReviewServiceCallsDataServiceDelete() {
        // given
        Review review = TestFixtures.getReviewFixtures().getFirst();
        Long reviewId = review.getId();
        assertNotNull(reviewId);

        // when
        reviewService.delete(reviewId);

        // then
        verify(reviewDataService).delete(reviewId);
    }
}
