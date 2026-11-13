package app.template.extension.extension;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InstagramExtensionTest {
    private enum NavTab {
        FEED("fragment_feed"),
        SEARCH("fragment_search"),
        CREATE("fragment_share"),
        REELS("fragment_clips"),
        PROFILE("fragment_profile");

        @SuppressWarnings("unused")
        private final String fragmentName;

        NavTab(String fragmentName) {
            this.fragmentName = fragmentName;
        }
    }

    private static final class StoryItem {
        @SuppressWarnings("unused")
        private final String reelType;

        StoryItem(String reelType) {
            this.reelType = reelType;
        }
    }

    public static void main(String[] args) throws Exception {
        // 1. LimitFeedToFollowedProfiles
        Map<String, String> defaultHeaders = Collections.singletonMap("pagination_source", "feed_recs");
        Map<String, String> patched = InstagramLimitFeedToFollowedProfiles.setFollowingHeader(defaultHeaders);
        check("following".equals(patched.get("pagination_source")), "feed_recs must become following");

        Map<String, String> emptyHeaders = Collections.singletonMap("other", "value");
        Map<String, String> patchedEmpty = InstagramLimitFeedToFollowedProfiles.setFollowingHeader(emptyHeaders);
        check("following".equals(patchedEmpty.get("pagination_source")), "missing header must become following");

        Map<String, String> favoritesHeaders = Collections.singletonMap("pagination_source", "favorites");
        check(
            favoritesHeaders == InstagramLimitFeedToFollowedProfiles.setFollowingHeader(favoritesHeaders),
            "non-feed_recs header must pass through unchanged"
        );
        check(
            InstagramLimitFeedToFollowedProfiles.setFollowingHeader(null) == null,
            "null map must return null"
        );

        // 2. HideNavigationButtonsPatch
        List<Object> tabs = Arrays.asList(NavTab.values());
        List<Object> withoutReels = InstagramHideNavigationButtonsPatch.removeNavigationButtonByName(
            tabs, "fragment_clips", "fragmentName"
        );
        check(withoutReels.size() == 4 && !withoutReels.contains(NavTab.REELS), "REELS tab must be removed");
        List<Object> withoutCreate = InstagramHideNavigationButtonsPatch.removeNavigationButtonByName(
            withoutReels, "fragment_share", "fragmentName"
        );
        check(withoutCreate.size() == 3 && !withoutCreate.contains(NavTab.CREATE), "CREATE tab must be removed");

        // 3. FilterStoriesListPatch
        InstagramFilterStoriesListPatch.BLOCKED_STORY_TYPES.add("suggested_user_reel");
        InstagramFilterStoriesListPatch.BLOCKED_STORY_TYPES.add("highlight_rewind_reel");
        List<Object> out = new ArrayList<>();
        InstagramFilterStoriesListPatch.addStoryIfNotBlocked(out, new StoryItem("user_reel"), "reelType");
        InstagramFilterStoriesListPatch.addStoryIfNotBlocked(out, new StoryItem("suggested_user_reel"), "reelType");
        InstagramFilterStoriesListPatch.addStoryIfNotBlocked(out, new StoryItem("highlight_rewind_reel"), "reelType");
        InstagramFilterStoriesListPatch.addStoryIfNotBlocked(out, new StoryItem(null), "reelType");
        check(out.size() == 2, "blocked story types must be dropped while keeping user_reel & null");

        List<Object> bulk = Arrays.asList(
            new StoryItem("user_reel"),
            new StoryItem("suggested_user_reel"),
            new StoryItem("BOGUS")
        );
        List<Object> filteredBulk = InstagramFilterStoriesListPatch.removeSuggestedStories(bulk, "reelType");
        check(filteredBulk.size() == 2, "sentinel BOGUS must not be filtered");

        // 4. BlockUrlRequestPatch
        InstagramBlockUrlRequestPatch.BLOCKED_URLS.add("/discover/ayml");
        InstagramBlockUrlRequestPatch.checkAndBlockUrl(URI.create("https://i.instagram.com/api/v1/feed/timeline/"));
        InstagramBlockUrlRequestPatch.checkAndBlockUrl(URI.create("https://i.instagram.com/BOGUS"));
        InstagramBlockUrlRequestPatch.checkAndBlockUrl(null);
        boolean threw = false;
        try {
            InstagramBlockUrlRequestPatch.checkAndBlockUrl(URI.create("https://i.instagram.com/api/v1/discover/ayml/"));
        } catch (IOException expected) {
            threw = true;
        }
        check(threw, "blocked URL substring must throw IOException");

        // 5. OverrideMobileConfigBooleanFlagPatch
        InstagramOverrideMobileConfigBooleanFlagPatch.OVERRIDES.put("83371::0", Boolean.FALSE);
        InstagramOverrideMobileConfigBooleanFlagPatch.OVERRIDES.put("81702::5", Boolean.TRUE);
        check(
            Boolean.FALSE.equals(InstagramOverrideMobileConfigBooleanFlagPatch.overrideBooleanFlag(0L, "83371", "0")),
            "override false must match"
        );
        check(
            Boolean.TRUE.equals(InstagramOverrideMobileConfigBooleanFlagPatch.overrideBooleanFlag(0L, "81702", "5")),
            "override true must match"
        );
        check(
            InstagramOverrideMobileConfigBooleanFlagPatch.overrideBooleanFlag(0L, "99999", "1") == null,
            "unconfigured flag must return null"
        );
        check(
            InstagramOverrideMobileConfigBooleanFlagPatch.overrideBooleanFlag(0L, "BOG", "US") == null,
            "non-matching id must return null"
        );
        long stdSpecifier = (1L << 62) | (0x1234L << 16);
        check(InstagramOverrideMobileConfigBooleanFlagPatch.getMobileConfigFlagId(stdSpecifier) == 0x1234L, "16-bit flag id");
        long smallSpecifier = (0xabcL << 16);
        check(InstagramOverrideMobileConfigBooleanFlagPatch.getMobileConfigFlagId(smallSpecifier) == 0xabcL, "12-bit flag id");

        System.out.println("Instagram extension helper tests passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
