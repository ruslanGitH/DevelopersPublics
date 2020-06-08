package main.com.skillbox.ru.developerspublics.model;

import lombok.*;
import javax.persistence.*;
import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "post_votes")
@Entity
public class PostVote
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "post_id", nullable = false)
    private int postId;

    @Column(nullable = false)
    private Date time;

    @Column(columnDefinition = "TINYINT", nullable = false)
    private int value;  // +1 -> like, -1 -> dislike

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User userVote;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post postVote;
}