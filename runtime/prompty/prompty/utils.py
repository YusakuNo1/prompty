import json
import os
import re
import sys
from typing import Any, Dict, Union
from pathlib import Path

import aiofiles
import yaml

_yaml_regex = re.compile(
    r"^\s*" + r"(?:---|\+\+\+)" + r"(.*?)" + r"(?:---|\+\+\+)" + r"\s*(.+)$",
    re.S | re.M,
)


def load_text(file_path, encoding="utf-8"):
    with open(file_path, encoding=encoding) as file:
        return file.read()


async def load_text_async(file_path, encoding="utf-8"):
    async with aiofiles.open(file_path, encoding=encoding) as f:
        content = await f.read()
        return content


def load_json(file_path, encoding="utf-8"):
    return json.loads(load_text(file_path, encoding=encoding))


async def load_json_async(file_path, encoding="utf-8"):
    # async file open
    content = await load_text_async(file_path, encoding=encoding)
    return json.loads(content)


def _find_global_config(prompty_path: Path = Path.cwd()) -> Union[Path, None]:
    prompty_config = list(Path.cwd().glob("**/prompty.json"))

    if len(prompty_config) > 0:
        return sorted(
            [
                c
                for c in prompty_config
                if len(c.parent.parts) <= len(prompty_path.parts)
            ],
            key=lambda p: len(p.parts),
        )[-1]
    else:
        return None


def load_global_config(
    prompty_path: Path = Path.cwd(), configuration: str = "default"
) -> dict[str, Any]:
    # prompty.config laying around?
    config = _find_global_config(prompty_path)

    # if there is one load it
    if config is not None:
        c = load_json(config)
        if configuration in c:
            return c[configuration]
        else:
            raise ValueError(f'Item "{configuration}" not found in "{config}"')

    return {}


async def load_global_config_async(
    prompty_path: Path = Path.cwd(), configuration: str = "default"
) -> dict[str, Any]:
    # prompty.config laying around?
    config = _find_global_config(prompty_path)

    # if there is one load it
    if config is not None:
        c = await load_json_async(config)
        if configuration in c:
            return c[configuration]
        else:
            raise ValueError(f'Item "{configuration}" not found in "{config}"')

    return {}


def load_prompty(file_path, encoding="utf-8"):
    contents = load_text(file_path, encoding=encoding)
    return parse(contents)


async def load_prompty_async(file_path, encoding="utf-8"):
    contents = await load_text_async(file_path, encoding=encoding)
    return parse(contents)


def parse(contents):
    global _yaml_regex

    fmatter = ""
    body = ""
    result = _yaml_regex.search(contents)

    if result:
        fmatter = result.group(1)
        body = result.group(2)
    return {
        "attributes": yaml.load(fmatter, Loader=yaml.FullLoader),
        "body": body,
        "frontmatter": fmatter,
    }


def remove_leading_empty_space(multiline_str: str) -> str:
    """
    Processes a multiline string by:
    1. Removing empty lines
    2. Finding the minimum leading spaces
    3. Indenting all lines to the minimum level

    :param multiline_str: The input multiline string.
    :type multiline_str: str
    :return: The processed multiline string.
    :rtype: str
    """
    lines = multiline_str.splitlines()
    start_index = 0
    while start_index < len(lines) and lines[start_index].strip() == "":
        start_index += 1

    # Find the minimum number of leading spaces
    min_spaces = sys.maxsize
    for line in lines[start_index:]:
        if len(line.strip()) == 0:
            continue
        spaces = len(line) - len(line.lstrip())
        spaces += line.lstrip().count("\t") * 2  # Count tabs as 2 spaces
        min_spaces = min(min_spaces, spaces)

    # Remove leading spaces and indent to the minimum level
    processed_lines = []
    for line in lines[start_index:]:
        processed_lines.append(line[min_spaces:])

    return "\n".join(processed_lines)


# clean up key value pairs for sensitive values
def sanitize(key: str, value: Any) -> Any:
    if isinstance(value, str) and any(
        [s in key.lower() for s in ["key", "secret", "password", "credential"]]
    ):
        return 10 * "*"
    elif isinstance(value, dict):
        return {k: sanitize(k, v) for k, v in value.items()}
    else:
        return value