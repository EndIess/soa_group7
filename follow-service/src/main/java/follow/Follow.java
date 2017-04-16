package follow;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by nut on 15/4/2560.
 */

@Document(collection = "follows")
public class Follow {
    @Id
    private String id;

    long userId;

    public Follow(long userId, String profilePicture, String displayName) {
        this.profilePicture = profilePicture;
        this.displayName = displayName;
    }

    private String profilePicture;

    private String displayName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
