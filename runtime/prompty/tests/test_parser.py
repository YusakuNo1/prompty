import prompty
import json
from prompty.core import Prompty
from prompty.parsers import PromptyChatParser

roles = ["assistant", "function", "system", "user"]


# def test_parse_with_args():
#     content = 'system[key="value 1", post=false, great=True, other=3.2, pre = 2]:\nYou are an AI assistant\n who helps people find information.\nAs the assistant, you answer questions briefly, succinctly.\n\nuser:\nWhat is the meaning of life?'
#     parser = PromptyChatParser(Prompty())
#     messages = parser.invoke(content)
#     assert len(messages) == 2
#     assert messages[0]["role"] == "system"
#     assert messages[0]["key"] == "value 1"
#     assert messages[0]["post"] is False
#     assert messages[0]["great"] is True
#     assert messages[0]["other"] == 3.2
#     assert messages[0]["pre"] == 2
#     assert messages[1]["role"] == "user"


# def test_parse_invalid_args():
#     content = 'system[role="value 1", content="overwrite content",post=false, great=True, other=3.2, pre = 2]:\nYou are an AI assistant\n who helps people find information.\nAs the assistant, you answer questions briefly, succinctly.\n\nuser:\nWhat is the meaning of life?'
#     parser = PromptyChatParser(Prompty())
#     messages = parser.invoke(content)
#     assert len(messages) == 2
#     assert messages[0]["role"] == "system"
#     assert (
#         messages[0]["content"]
#         == "You are an AI assistant\n who helps people find information.\nAs the assistant, you answer questions briefly, succinctly.\n"
#     )
#     assert messages[1]["role"] == "user"


# def test_thread_parse():
#     p = prompty.load("tools/basic.prompty")
#     content = prompty.prepare(p, merge_sample=True)
#     assert len(content) == 3
#     assert content[0]["role"] == "system"
#     assert content[1]["role"] == "thread"
#     assert content[2]["role"] == "system"


def test_david():
    # p = prompty.load("prompts/basic.prompty")
    # inputs = {
    #     "firstName": "Jack",
    #     "lastName": "Brown",
    #     "question": "OTHER_TEXT_OTHER_TEXT",
    # }
    # content = prompty.prepare(
    #     p,
    #     inputs=inputs,
    #     merge_sample=True,
    # )

    p = prompty.load("prompts/chat.prompty")
    inputs = {
        "firstName": "Jack",
        "lastName": "Brown",
        "input": "OTHER_TEXT_OTHER_INPUT",
        "chat_history": [
            {
                "role": "user",
                "content": [
                    {
                        "type": "image_url",
                        "image_url": {
                            "url": "data:image/png;base64,mocked_image_data"
                        }
                    }
                ]
            },
            {
                "role": "assistant",
                "content": [
                    {
                        "type": "text",
                        "text": "This is a logo of Starbucks."
                    }
                ],
            },
        ],
    }
    content = prompty.prepare(
        p,
        inputs=inputs,
    )

    print("******************************************")
    json_content = json.dumps(content, indent=2)
    print(json_content)
