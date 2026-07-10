package com.daisy.health.controller;

import com.daisy.health.common.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class UploadControllerTest {
    @TempDir
    Path uploadDir;

    @Test
    void validPngAvatarCanBeUploaded() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", pngContent(32));

        mockMvc().perform(multipart("/api/v1/uploads")
                        .file(file)
                        .param("category", "avatar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.url").value(org.hamcrest.Matchers.startsWith("/uploads/avatar/")));
    }

    @Test
    void emptyAvatarReturnsReadableError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[0]);

        mockMvc().perform(multipart("/api/v1/uploads")
                        .file(file)
                        .param("category", "avatar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1005))
                .andExpect(jsonPath("$.message").value("请选择要上传的文件"));
    }

    @Test
    void unsupportedAvatarExtensionReturnsReadableError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.gif", "image/gif", "GIF89a".getBytes("UTF-8"));

        mockMvc().perform(multipart("/api/v1/uploads")
                        .file(file)
                        .param("category", "avatar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1005))
                .andExpect(jsonPath("$.message").value("文件格式不支持"));
    }

    @Test
    void renamedTextFileIsRejectedAsAvatar() throws Exception {
        MockMvc mockMvc = mockMvc();
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", "not-an-image".getBytes("UTF-8")
        );

        mockMvc.perform(multipart("/api/v1/uploads")
                        .file(file)
                        .param("category", "avatar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1005))
                .andExpect(jsonPath("$.message").value("文件格式不支持"));
    }

    @Test
    void oversizedAvatarReturnsReadableError() throws Exception {
        MockMvc mockMvc = mockMvc();
        byte[] content = new byte[2 * 1024 * 1024 + 1];
        content[0] = (byte) 0x89;
        content[1] = 0x50;
        content[2] = 0x4e;
        content[3] = 0x47;
        content[4] = 0x0d;
        content[5] = 0x0a;
        content[6] = 0x1a;
        content[7] = 0x0a;
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", content);

        mockMvc.perform(multipart("/api/v1/uploads")
                        .file(file)
                        .param("category", "avatar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1005))
                .andExpect(jsonPath("$.message").value("头像文件不能超过 2MB"));
    }

    private MockMvc mockMvc() {
        return standaloneSetup(new UploadController(uploadDir.toString()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private byte[] pngContent(int size) {
        byte[] content = new byte[size];
        content[0] = (byte) 0x89;
        content[1] = 0x50;
        content[2] = 0x4e;
        content[3] = 0x47;
        content[4] = 0x0d;
        content[5] = 0x0a;
        content[6] = 0x1a;
        content[7] = 0x0a;
        return content;
    }
}
