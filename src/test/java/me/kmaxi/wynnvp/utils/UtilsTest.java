package me.kmaxi.wynnvp.utils;

import me.kmaxi.wynnvp.Config;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Utils Tests")
class UtilsTest {

    @Mock
    private Member member;

    @Mock
    private Role voiceManagerRole;

    @Mock
    private Role writeRole;

    @Mock
    private Role otherRole;

    @Test
    @DisplayName("Should throw UnsupportedOperationException when attempting to instantiate")
    void constructor_ThrowsException() {
        assertThatThrownBy(() -> {
            var constructor = Utils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        })
        .hasCauseInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("This is a utility class and cannot be instantiated");
    }

    @Test
    @DisplayName("Should return true when member has Voice Manager role")
    void isStaff_VoiceManager_ReturnsTrue() {
        // Given
        when(voiceManagerRole.getIdLong()).thenReturn(Config.VOICE_MANGER_ID);
        when(member.getRoles()).thenReturn(Arrays.asList(otherRole, voiceManagerRole));

        // When
        boolean result = Utils.isStaff(member);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return true when member has Write role")
    void isStaff_WriteRole_ReturnsTrue() {
        // Given
        when(writeRole.getIdLong()).thenReturn(Config.WRITE_ROLE_ID);
        when(member.getRoles()).thenReturn(Arrays.asList(otherRole, writeRole));

        // When
        boolean result = Utils.isStaff(member);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when member has neither staff role")
    void isStaff_NoStaffRoles_ReturnsFalse() {
        // Given
        when(otherRole.getIdLong()).thenReturn(999999L);
        when(member.getRoles()).thenReturn(Collections.singletonList(otherRole));

        // When
        boolean result = Utils.isStaff(member);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return true when member has administrator permission")
    void isAdmin_HasPermission_ReturnsTrue() {
        // Given
        when(member.hasPermission(Permission.ADMINISTRATOR)).thenReturn(true);

        // When
        boolean result = Utils.isAdmin(member);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when member does not have administrator permission")
    void isAdmin_NoPermission_ReturnsFalse() {
        // Given
        when(member.hasPermission(Permission.ADMINISTRATOR)).thenReturn(false);

        // When
        boolean result = Utils.isAdmin(member);

        // Then
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
        "'my channel name', 'mychannelname'",
        "'Test Channel!', 'TestChannel'",
        "'role-123', 'role-123'",
        "'Channel @#$% Name', 'ChannelName'",
        "'123-abc-456', '123-abc-456'",
        "'!!!', ''",
        "'Test_Channel', 'TestChannel'"
    })
    @DisplayName("Should sanitize channel names by removing invalid characters")
    void getChannelName_VariousInputs_RemovesInvalidCharacters(String input, String expected) {
        // When
        String result = Utils.getChannelName(input);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "1, one",
        "2, two",
        "3, three",
        "4, four",
        "5, five",
        "6, six",
        "7, seven",
        "8, eight",
        "9, nine",
        "0, :x:",
        "10, :x:",
        "-1, :x:"
    })
    @DisplayName("Should convert numbers 1-9 to words")
    void convertNumber_ValidNumbers_ReturnsWords(int number, String expected) {
        // When
        String result = Utils.convertNumber(number);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "1, regional_indicator_a",
        "2, regional_indicator_b",
        "5, regional_indicator_e",
        "26, regional_indicator_z",
        "0, x",
        "27, x",
        "-1, x"
    })
    @DisplayName("Should convert alphabetical order to regional indicator emoji names")
    void convertLetter_ValidNumbers_ReturnsIndicators(int alphabeticalOrder, String expected) {
        // When
        String result = Utils.convertLetter(alphabeticalOrder);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "1, 1️⃣",
        "2, 2️⃣",
        "3, 3️⃣",
        "4, 4️⃣",
        "5, 5️⃣",
        "6, 6️⃣",
        "7, 7️⃣",
        "8, 8️⃣",
        "9, 9️⃣",
        "0, ❌",
        "10, ❌"
    })
    @DisplayName("Should convert numbers to unicode emoji")
    void getUnicode_Numbers_ReturnsEmoji(int number, String expected) {
        // When
        String result = Utils.getUnicode(number);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "A, 🇦",
        "B, 🇧",
        "C, 🇨",
        "M, 🇲",
        "Z, 🇿"
    })
    @DisplayName("Should convert letters to unicode flag emoji")
    void getUnicode_Letters_ReturnsEmoji(char letter, String expected) {
        // When
        String result = Utils.getUnicode(letter);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return error emoji for invalid letter")
    void getUnicode_InvalidLetter_ReturnsErrorEmoji() {
        // When
        String result = Utils.getUnicode('a'); // lowercase

        // Then
        assertThat(result).isEqualTo("❌");
    }

    @ParameterizedTest
    @CsvSource({
        "1️⃣, 1",
        "2️⃣, 2",
        "3️⃣, 3",
        "4️⃣, 4",
        "5️⃣, 5",
        "6️⃣, 6",
        "7️⃣, 7",
        "8️⃣, 8",
        "9️⃣, 9",
        "❌, 0",
        "🎉, 0"
    })
    @DisplayName("Should convert emoji back to number")
    void whichNumberWasReacted_ValidEmojis_ReturnsNumber(String emoji, int expected) {
        // When
        int result = Utils.whichNumberWasReacted(emoji);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "🇦, A",
        "🇧, B",
        "🇨, C",
        "🇲, M",
        "🇿, Z"
    })
    @DisplayName("Should convert emoji back to letter")
    void whichLetterWasReacted_ValidEmojis_ReturnsLetter(String emoji, char expected) {
        // When
        char result = Utils.whichLetterWasReacted(emoji);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return 0 for invalid letter emoji")
    void whichLetterWasReacted_InvalidEmoji_ReturnsZero() {
        // When
        char result = Utils.whichLetterWasReacted("🎉");

        // Then
        assertThat(result).isEqualTo((char) 0);
    }

    @Test
    @DisplayName("Should return standard permissions collection")
    void permissions_ReturnsCorrectPermissions() {
        // When
        Collection<Permission> result = Utils.permissions();

        // Then
        assertThat(result)
                .hasSize(7)
                .contains(
                        Permission.MESSAGE_SEND,
                        Permission.MESSAGE_HISTORY,
                        Permission.MESSAGE_EMBED_LINKS,
                        Permission.MESSAGE_ATTACH_FILES,
                        Permission.MESSAGE_ADD_REACTION,
                        Permission.MESSAGE_EXT_EMOJI,
                        Permission.VIEW_CHANNEL
                );
    }

    @Test
    @DisplayName("Should return trainee permissions collection")
    void traineePerms_ReturnsCorrectPermissions() {
        // When
        Collection<Permission> result = Utils.traineePerms();

        // Then
        assertThat(result)
                .hasSize(2)
                .contains(
                        Permission.MESSAGE_HISTORY,
                        Permission.MESSAGE_ADD_REACTION
                );
    }

    @Test
    @DisplayName("Should verify emoji number conversion is bidirectional")
    void emojiNumberConversion_Bidirectional() {
        // Test that converting to emoji and back gives the same number
        for (int i = 1; i <= 9; i++) {
            String emoji = Utils.getUnicode(i);
            int number = Utils.whichNumberWasReacted(emoji);
            assertThat(number).isEqualTo(i);
        }
    }

    @Test
    @DisplayName("Should verify emoji letter conversion is bidirectional")
    void emojiLetterConversion_Bidirectional() {
        // Test that converting to emoji and back gives the same letter
        for (char c = 'A'; c <= 'Z'; c++) {
            String emoji = Utils.getUnicode(c);
            char letter = Utils.whichLetterWasReacted(emoji);
            assertThat(letter).isEqualTo(c);
        }
    }

    @Test
    @DisplayName("Should handle empty string in channel name sanitization")
    void getChannelName_EmptyString_ReturnsEmpty() {
        // When
        String result = Utils.getChannelName("");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should preserve hyphens in channel names")
    void getChannelName_WithHyphens_PreservesHyphens() {
        // When
        String result = Utils.getChannelName("test-channel-name");

        // Then
        assertThat(result).isEqualTo("test-channel-name");
    }

    @Test
    @DisplayName("Should preserve alphanumeric characters in channel names")
    void getChannelName_AlphanumericOnly_Preserved() {
        // When
        String result = Utils.getChannelName("Channel123");

        // Then
        assertThat(result).isEqualTo("Channel123");
    }
}
